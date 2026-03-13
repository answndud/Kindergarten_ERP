package com.erp.global.security;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientIpResolver {

    private static final String UNKNOWN = "unknown";

    private final ClientIpProperties properties;

    public String resolve(HttpServletRequest request) {
        String remoteAddr = normalizeIp(request.getRemoteAddr());
        if (!isTrustedProxy(remoteAddr)) {
            return fallback(remoteAddr);
        }

        String forwardedFor = extractForwardedFor(request.getHeader("X-Forwarded-For"));
        if (forwardedFor != null) {
            return forwardedFor;
        }

        String realIp = normalizeIp(request.getHeader("X-Real-IP"));
        if (realIp != null) {
            return realIp;
        }

        return fallback(remoteAddr);
    }

    private boolean isTrustedProxy(String remoteAddr) {
        if (remoteAddr == null) {
            return false;
        }
        if (properties.getTrustedProxies().contains(remoteAddr)) {
            return true;
        }

        try {
            return InetAddress.getByName(remoteAddr).isLoopbackAddress();
        } catch (Exception ignored) {
            return "127.0.0.1".equals(remoteAddr)
                    || "::1".equals(remoteAddr)
                    || "0:0:0:0:0:0:0:1".equals(remoteAddr);
        }
    }

    private String extractForwardedFor(String forwardedForHeader) {
        if (forwardedForHeader == null || forwardedForHeader.isBlank()) {
            return null;
        }

        String[] candidates = forwardedForHeader.split(",");
        for (String candidate : candidates) {
            String normalized = normalizeIp(candidate);
            if (normalized != null && !UNKNOWN.equalsIgnoreCase(normalized)) {
                return normalized;
            }
        }
        return null;
    }

    private String normalizeIp(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String fallback(String remoteAddr) {
        return remoteAddr == null ? UNKNOWN : remoteAddr;
    }
}
