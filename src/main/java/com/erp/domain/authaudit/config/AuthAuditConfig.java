package com.erp.domain.authaudit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AuthAlertProperties.class)
public class AuthAuditConfig {
}
