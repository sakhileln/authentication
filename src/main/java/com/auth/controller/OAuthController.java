package com.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/oauth")
@CrossOrigin(origins = "*")
public class OAuthController {

    @GetMapping("/{provider}")
    public ResponseEntity<Map<String, String>> redirectToProvider(@PathVariable String provider) {
        // This would typically redirect to the OAuth provider
        // For now, we'll return a message indicating the redirect
        Map<String, String> response = new HashMap<>();
        response.put("message", "Redirecting to " + provider + " OAuth provider");
        response.put("provider", provider);
        response.put("redirectUrl", "/oauth2/authorization/" + provider);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{provider}/callback")
    public ResponseEntity<Map<String, String>> handleOAuthCallback(
            @PathVariable String provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error) {

        Map<String, String> response = new HashMap<>();

        if (error != null) {
            response.put("error", "OAuth authentication failed: " + error);
            return ResponseEntity.badRequest().body(response);
        }

        if (code == null) {
            response.put("error", "Authorization code not received");
            return ResponseEntity.badRequest().body(response);
        }

        // This would typically exchange the code for tokens and create/update user
        response.put("message", "OAuth authentication successful");
        response.put("provider", provider);
        response.put("code", code);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> exchangeCodeForToken(
            @RequestParam String provider,
            @RequestParam String code) {

        // This would exchange the authorization code for access tokens
        Map<String, String> response = new HashMap<>();
        response.put("message", "Token exchange successful");
        response.put("provider", provider);
        response.put("accessToken", "oauth_access_token_placeholder");
        response.put("refreshToken", "oauth_refresh_token_placeholder");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/link")
    public ResponseEntity<Map<String, String>> linkOAuthProvider(
            Authentication authentication,
            @RequestParam String provider,
            @RequestParam String accessToken) {

        if (authentication != null && authentication.getPrincipal() instanceof com.auth.entity.User) {
            com.auth.entity.User user = (com.auth.entity.User) authentication.getPrincipal();

            // This would link the OAuth provider to the existing user account
            // user.getOauthProviders().add(provider);
            // userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "OAuth provider linked successfully");
            response.put("provider", provider);
            response.put("userId", user.getId().toString());

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/unlink")
    public ResponseEntity<Map<String, String>> unlinkOAuthProvider(
            Authentication authentication,
            @RequestParam String provider) {

        if (authentication != null && authentication.getPrincipal() instanceof com.auth.entity.User) {
            com.auth.entity.User user = (com.auth.entity.User) authentication.getPrincipal();

            // This would unlink the OAuth provider from the user account
            // user.getOauthProviders().remove(provider);
            // userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "OAuth provider unlinked successfully");
            response.put("provider", provider);
            response.put("userId", user.getId().toString());

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().build();
    }
}