package com.erp.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정
 * JPA Auditing을 활성화합니다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
