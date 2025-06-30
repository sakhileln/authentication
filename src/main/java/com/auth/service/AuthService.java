package com.auth.service;

import com.auth.dto.AuthResponse;
import com.auth.dto.LoginRequest;
import com.auth.dto.SignupRequest;
import com.auth.entity.*;
import com.auth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${app.auth.email.verification.token-validity:3600}")
    private long emailVerificationTokenValidity;

    @Value("${app.auth.password-reset.token-validity:3600}")
    private long passwordResetTokenValidity;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            User user = (User) authentication.getPrincipal();

            // Check if MFA is required
            if (user.isMfaEnabled()) {
                if (request.getMfaCode() == null || request.getMfaCode().isEmpty()) {
                    // MFA is enabled but no code provided
                    return createMfaRequiredResponse(user);
                }

                // Verify MFA code
                if (!mfaService.verifyCode(request.getMfaCode(), user.getMfaSecret())) {
                    throw new BadCredentialsException("Invalid MFA code");
                }
            }

            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);

            return new AuthResponse(accessToken, refreshToken, convertToUserDto(user));

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmailVerified(false);

        user = userRepository.save(user);

        // Send email verification
        sendEmailVerification(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, convertToUserDto(user));
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.isExpired() || token.isRevoked()) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        User user = token.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        // Revoke old refresh token
        token.setRevokedAt(LocalDateTime.now());
        token.setRevokedBy("refresh");
        refreshTokenRepository.save(token);

        return new AuthResponse(newAccessToken, newRefreshToken, convertToUserDto(user));
    }

    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElse(null);

        if (token != null) {
            token.setRevokedAt(LocalDateTime.now());
            token.setRevokedBy("logout");
            refreshTokenRepository.save(token);
        }
    }

    @Transactional
    public void revokeAllTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.revokeAllTokensForUser(user, LocalDateTime.now(), "revoke_all");
    }

    @Transactional
    public void requestEmailVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        sendEmailVerification(user);
    }

    @Transactional
    public void confirmEmailVerification(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new RuntimeException("Verification token expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        // Delete the verification token
        emailVerificationTokenRepository.delete(verificationToken);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create password reset token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusSeconds(passwordResetTokenValidity));
        passwordResetTokenRepository.save(resetToken);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken(), user.getUsername());
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new RuntimeException("Reset token expired or already used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String generateRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    private void sendEmailVerification(User user) {
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(LocalDateTime.now().plusSeconds(emailVerificationTokenValidity));
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendEmailVerification(user.getEmail(), verificationToken.getToken(), user.getUsername());
    }

    private AuthResponse createMfaRequiredResponse(User user) {
        return new AuthResponse(null, null, convertToUserDto(user), true);
    }

    private AuthResponse.UserDto convertToUserDto(User user) {
        AuthResponse.UserDto userDto = new AuthResponse.UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmailVerified(user.isEmailVerified());
        userDto.setMfaEnabled(user.isMfaEnabled());
        return userDto;
    }
}