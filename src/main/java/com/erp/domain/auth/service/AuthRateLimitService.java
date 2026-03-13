package com.erp.domain.auth.service;

import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 인증 API 남용을 막기 위한 단순 Redis 기반 rate limit 서비스
 */
@Service
@RequiredArgsConstructor
public class AuthRateLimitService {

    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(10);
    private static final Duration REFRESH_WINDOW = Duration.ofMinutes(5);

    private static final long LOGIN_IP_LIMIT = 15L;
    private static final long LOGIN_EMAIL_LIMIT = 5L;
    private static final long REFRESH_IP_LIMIT = 10L;

    private static final String LOGIN_IP_KEY_PREFIX = "rate-limit:auth:login:ip:";
    private static final String LOGIN_EMAIL_KEY_PREFIX = "rate-limit:auth:login:email:";
    private static final String REFRESH_IP_KEY_PREFIX = "rate-limit:auth:refresh:ip:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void validateLoginAllowed(String clientIp, String email) {
        consumeSlot(
                LOGIN_IP_KEY_PREFIX + normalizeClientIp(clientIp),
                LOGIN_IP_LIMIT,
                LOGIN_WINDOW
        );
        consumeSlot(
                LOGIN_EMAIL_KEY_PREFIX + normalizeEmail(email),
                LOGIN_EMAIL_LIMIT,
                LOGIN_WINDOW
        );
    }

    public void validateRefreshAllowed(String clientIp) {
        consumeSlot(
                REFRESH_IP_KEY_PREFIX + normalizeClientIp(clientIp),
                REFRESH_IP_LIMIT,
                REFRESH_WINDOW
        );
    }

    private void consumeSlot(String key, long limit, Duration window) {
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(key, window);
        }

        if (attempts != null && attempts > limit) {
            throw new BusinessException(ErrorCode.AUTH_RATE_LIMITED);
        }
    }

    private String normalizeClientIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "unknown";
        }
        return clientIp.trim();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "anonymous";
        }
        return email.trim().toLowerCase();
    }
}
