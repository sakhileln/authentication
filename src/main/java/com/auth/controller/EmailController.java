package com.auth.controller;

import com.auth.service.AuthService;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/email")
@CrossOrigin(origins = "*")
public class EmailController {

    @Autowired
    private AuthService authService;

    @PostMapping("/verify/request")
    public ResponseEntity<Map<String, String>> requestEmailVerification(@RequestParam @Email String email) {
        authService.requestEmailVerification(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email verification link sent successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify/confirm")
    public ResponseEntity<Map<String, String>> confirmEmailVerification(@RequestParam String token) {
        authService.confirmEmailVerification(token);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email verified successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify/status")
    public ResponseEntity<Map<String, Boolean>> getEmailVerificationStatus(@RequestParam @Email String email) {
        // This would typically check the database for email verification status
        // For now, we'll return a simple response
        Map<String, Boolean> response = new HashMap<>();
        response.put("verified", false); // This should be implemented based on actual user data
        return ResponseEntity.ok(response);
    }
}