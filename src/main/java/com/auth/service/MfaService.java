package com.auth.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MfaService {

    @Value("${app.auth.mfa.backup-codes-count:10}")
    private int backupCodesCount;

    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String generateQrCode(String secret, String email, String issuer) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        byte[] qrCodeBytes = qrGenerator.generate(data);
        return Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    public boolean verifyCode(String code, String secret) {
        return codeVerifier.isValidCode(secret, code);
    }

    public Set<String> generateBackupCodes() {
        Set<String> backupCodes = new HashSet<>();
        for (int i = 0; i < backupCodesCount; i++) {
            backupCodes.add(generateBackupCode());
        }
        return backupCodes;
    }

    private String generateBackupCode() {
        // Generate 8-character alphanumeric backup code
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            code.append(chars.charAt(index));
        }
        return code.toString();
    }

    public boolean verifyBackupCode(String code, Set<String> backupCodes) {
        return backupCodes.contains(code);
    }

    public Set<String> removeUsedBackupCode(String usedCode, Set<String> backupCodes) {
        return backupCodes.stream()
                .filter(code -> !code.equals(usedCode))
                .collect(Collectors.toSet());
    }

    public String getQrCodeUrl(String secret, String email, String issuer) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuer, email, secret, issuer);
    }
}