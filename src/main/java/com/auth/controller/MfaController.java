package com.auth.controller;

import com.auth.entity.User;
import com.auth.repository.UserRepository;
import com.auth.service.AuthService;
import com.auth.service.MfaService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth/mfa")
@CrossOrigin(origins = "*")
public class MfaController {

    @Autowired
    private MfaService mfaService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/setup")
    public ResponseEntity<Map<String, Object>> setupMfa(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        String secret = mfaService.generateSecret();
        String qrCodeUrl = mfaService.getQrCodeUrl(secret, user.getEmail(), "Authentication Service");

        Map<String, Object> response = new HashMap<>();
        response.put("secret", secret);
        response.put("qrCodeUrl", qrCodeUrl);
        response.put("message", "MFA setup initiated. Scan the QR code with your authenticator app.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyMfaSetup(
            Authentication authentication,
            @RequestParam @NotBlank String code,
            @RequestParam @NotBlank String secret) {

        User user = (User) authentication.getPrincipal();

        if (!mfaService.verifyCode(code, secret)) {
            throw new RuntimeException("Invalid MFA code");
        }

        // Enable MFA for user
        user.setMfaEnabled(true);
        user.setMfaSecret(secret);

        // Generate backup codes
        Set<String> backupCodes = mfaService.generateBackupCodes();
        user.setMfaBackupCodes(backupCodes);

        // Save user (this would be done through a service method in a real
        // implementation)
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "MFA setup completed successfully");
        response.put("backupCodes", String.join(", ", backupCodes));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/enable")
    public ResponseEntity<Map<String, String>> enableMfa(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if (user.getMfaSecret() == null) {
            throw new RuntimeException("MFA not set up. Please set up MFA first.");
        }

        user.setMfaEnabled(true);
        // Save user (this would be done through a service method in a real
        // implementation)
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "MFA enabled successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable")
    public ResponseEntity<Map<String, String>> disableMfa(
            Authentication authentication,
            @RequestParam @NotBlank String code) {

        User user = (User) authentication.getPrincipal();

        if (!mfaService.verifyCode(code, user.getMfaSecret())) {
            throw new RuntimeException("Invalid MFA code");
        }

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setMfaBackupCodes(null);
        // Save user (this would be done through a service method in a real
        // implementation)
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "MFA disabled successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/challenge")
    public ResponseEntity<Map<String, String>> challengeMfa(
            @RequestParam @NotBlank String email,
            @RequestParam @NotBlank String code) {

        // This endpoint would be used during login when MFA is required
        // For now, we'll return a simple response
        Map<String, String> response = new HashMap<>();
        response.put("message", "MFA challenge completed");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recovery")
    public ResponseEntity<Map<String, String>> useBackupCode(
            Authentication authentication,
            @RequestParam @NotBlank String backupCode) {

        User user = (User) authentication.getPrincipal();

        if (!mfaService.verifyBackupCode(backupCode, user.getMfaBackupCodes())) {
            throw new RuntimeException("Invalid backup code");
        }

        // Remove used backup code
        Set<String> updatedBackupCodes = mfaService.removeUsedBackupCode(backupCode, user.getMfaBackupCodes());
        user.setMfaBackupCodes(updatedBackupCodes);
        // Save user (this would be done through a service method in a real
        // implementation)
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Backup code used successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/backup-codes")
    public ResponseEntity<Map<String, Object>> getBackupCodes(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if (!user.isMfaEnabled()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "MFA not enabled");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("backupCodes", user.getMfaBackupCodes());
        response.put("remainingCodes", user.getMfaBackupCodes().size());

        return ResponseEntity.ok(response);
    }
}