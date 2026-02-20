package com.erp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 애플리케이션 컨텍스트 로드 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
class ErpApplicationTests {

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void contextLoads() {
        // 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인
    }
}
