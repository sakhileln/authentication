package com.auth.controller;

import com.auth.dto.AuthResponse;
import com.auth.dto.LoginRequest;
import com.auth.dto.SignupRequest;
import com.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully logged out");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/revoke")
    public ResponseEntity<Map<String, String>> revokeAllTokens(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        authService.revokeAllTokens(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "All tokens revoked successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> getCurrentUser(Authentication authentication) {
        // The user details are already available from the JWT token
        // This endpoint just returns the current user info
        if (authentication != null && authentication.getPrincipal() instanceof com.auth.entity.User) {
            com.auth.entity.User user = (com.auth.entity.User) authentication.getPrincipal();
            AuthResponse.UserDto userDto = convertToUserDto(user);
            return ResponseEntity.ok(userDto);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSessionInfo(Authentication authentication,
            HttpServletRequest request) {
        Map<String, Object> sessionInfo = new HashMap<>();

        if (authentication != null) {
            sessionInfo.put("authenticated", true);
            sessionInfo.put("user", authentication.getName());
            sessionInfo.put("authorities", authentication.getAuthorities());
        } else {
            sessionInfo.put("authenticated", false);
        }

        sessionInfo.put("remoteAddress", request.getRemoteAddr());
        sessionInfo.put("userAgent", request.getHeader("User-Agent"));

        return ResponseEntity.ok(sessionInfo);
    }

    private AuthResponse.UserDto convertToUserDto(com.auth.entity.User user) {
        AuthResponse.UserDto userDto = new AuthResponse.UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setProfileImageUrl(user.getProfileImageUrl());
        userDto.setEmailVerified(user.isEmailVerified());
        userDto.setEmailVerifiedAt(user.getEmailVerifiedAt());
        userDto.setLastLoginAt(user.getLastLoginAt());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setMfaEnabled(user.isMfaEnabled());
        return userDto;
    }
}