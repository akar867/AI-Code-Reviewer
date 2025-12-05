package com.aicode.reviewer.service.github;

import com.aicode.reviewer.config.GithubProperties;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubSignatureVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final GithubProperties properties;

    public boolean isSignatureValid(String payload, String signatureHeader) {
        if (!StringUtils.hasText(properties.getWebhookSecret())) {
            return true;
        }
        if (!StringUtils.hasText(payload) || !StringUtils.hasText(signatureHeader)) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(properties.getWebhookSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + bytesToHex(hmac);
            return slowEquals(expected, signatureHeader.trim());
        } catch (Exception ex) {
            log.warn("Unable to verify webhook signature: {}", ex.getMessage());
            return false;
        }
    }

    private boolean slowEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        if (aBytes.length != bBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
