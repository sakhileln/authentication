package com.auth.controller;

import com.auth.service.AuthService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/password")
@CrossOrigin(origins = "*")
public class PasswordController {

    @Autowired
    private AuthService authService;

    @PostMapping("/forgot")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestParam @Email String email) {
        authService.requestPasswordReset(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset email sent successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam String token,
            @RequestParam @NotBlank String newPassword) {
        authService.resetPassword(token, newPassword);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @RequestParam @NotBlank String currentPassword,
            @RequestParam @NotBlank String newPassword) {

        Long userId = Long.parseLong(authentication.getName());
        authService.changePassword(userId, currentPassword, newPassword);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
}