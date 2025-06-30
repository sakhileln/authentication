package com.auth.controller;

import com.auth.entity.User;
import com.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("firstName", user.getFirstName());
            userInfo.put("lastName", user.getLastName());
            userInfo.put("profileImageUrl", user.getProfileImageUrl());
            userInfo.put("emailVerified", user.isEmailVerified());
            userInfo.put("emailVerifiedAt", user.getEmailVerifiedAt());
            userInfo.put("lastLoginAt", user.getLastLoginAt());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("mfaEnabled", user.isMfaEnabled());
            userInfo.put("oauthProviders", user.getOauthProviders());

            return ResponseEntity.ok(userInfo);
        }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            Authentication authentication,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String profileImageUrl) {

        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();

            if (firstName != null) {
                user.setFirstName(firstName);
            }
            if (lastName != null) {
                user.setLastName(lastName);
            }
            if (profileImageUrl != null) {
                user.setProfileImageUrl(profileImageUrl);
            }

            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/email")
    public ResponseEntity<Map<String, String>> changeEmail(
            Authentication authentication,
            @RequestParam String newEmail,
            @RequestParam String password) {

        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();

            // Verify current password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }

            // Check if new email is already taken
            if (userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email already taken");
            }

            // Update email and mark as unverified
            user.setEmail(newEmail);
            user.setEmailVerified(false);
            user.setEmailVerifiedAt(null);

            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Email changed successfully. Please verify your new email.");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().build();
    }
}