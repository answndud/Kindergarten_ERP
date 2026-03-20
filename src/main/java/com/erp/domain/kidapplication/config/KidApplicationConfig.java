package com.erp.domain.kidapplication.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KidApplicationWorkflowProperties.class)
public class KidApplicationConfig {
}
