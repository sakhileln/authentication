package com.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.auth.email.verification.enabled:true}")
    private boolean emailVerificationEnabled;

    @Async
    public void sendEmailVerification(String to, String token, String username) {
        if (!emailVerificationEnabled) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Verify Your Email Address");
        message.setText(String.format(
                "Hello %s,\n\n" +
                        "Please verify your email address by clicking the link below:\n\n" +
                        "http://localhost:8080/auth/email/verify/confirm?token=%s\n\n" +
                        "This link will expire in 1 hour.\n\n" +
                        "If you didn't create an account, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Authentication Service",
                username, token));

        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Reset Your Password");
        message.setText(String.format(
                "Hello %s,\n\n" +
                        "You requested to reset your password. Click the link below to reset it:\n\n" +
                        "http://localhost:8080/auth/password/reset?token=%s\n\n" +
                        "This link will expire in 1 hour.\n\n" +
                        "If you didn't request a password reset, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Authentication Service",
                username, token));

        mailSender.send(message);
    }

    @Async
    public void sendWelcomeEmail(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Welcome to Our Service!");
        message.setText(String.format(
                "Hello %s,\n\n" +
                        "Welcome to our service! Your account has been successfully created.\n\n" +
                        "You can now log in and start using our platform.\n\n" +
                        "Best regards,\n" +
                        "Authentication Service",
                username));

        mailSender.send(message);
    }

    @Async
    public void sendMfaBackupCodesEmail(String to, String username, String backupCodes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Your MFA Backup Codes");
        message.setText(String.format(
                "Hello %s,\n\n" +
                        "Here are your MFA backup codes. Keep them safe in case you lose your MFA device:\n\n" +
                        "%s\n\n" +
                        "Each code can only be used once.\n\n" +
                        "Best regards,\n" +
                        "Authentication Service",
                username, backupCodes));

        mailSender.send(message);
    }
}