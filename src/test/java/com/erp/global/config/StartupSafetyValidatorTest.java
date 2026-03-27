package com.erp.global.config;

import com.erp.global.security.ManagementSurfaceProperties;
import com.erp.global.security.jwt.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StartupSafetyValidatorTest {

    @Test
    @DisplayName("활성 프로파일이 없으면 부팅을 막는다")
    void validate_Fails_WhenNoActiveProfile() {
        StartupSafetyValidator validator = newValidator(
                new String[0],
                "local-dev-only-jwt-secret-key-at-least-32-bytes",
                true,
                false,
                false,
                false,
                false
        );

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    @DisplayName("prod에서는 공개 API 문서를 허용하지 않는다")
    void validate_Fails_WhenProdExposesPublicApiDocs() {
        StartupSafetyValidator validator = newValidator(
                new String[]{"prod"},
                "prod-secret-key-at-least-32-bytes-long",
                true,
                true,
                false,
                false,
                false
        );

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    @DisplayName("prod에서는 seed를 켤 수 없다")
    void validate_Fails_WhenProdEnablesSeed() {
        StartupSafetyValidator validator = newValidator(
                new String[]{"prod"},
                "prod-secret-key-at-least-32-bytes-long",
                true,
                false,
                false,
                false,
                true
        );

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    @DisplayName("local에서는 명시적 프로파일과 개발용 fallback secret을 허용한다")
    void validate_Passes_ForLocalProfile() {
        StartupSafetyValidator validator = newValidator(
                new String[]{"local"},
                "local-dev-only-jwt-secret-key-at-least-32-bytes",
                false,
                true,
                true,
                true,
                false
        );

        assertDoesNotThrow(validator::validate);
    }

    private StartupSafetyValidator newValidator(String[] activeProfiles,
                                                String jwtSecret,
                                                boolean cookieSecure,
                                                boolean publicApiDocs,
                                                boolean exposePrometheusOnAppPort,
                                                boolean apiDocsEnabled,
                                                boolean seedEnabled) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(activeProfiles);
        environment.setProperty("springdoc.api-docs.enabled", Boolean.toString(apiDocsEnabled));
        environment.setProperty("springdoc.swagger-ui.enabled", Boolean.toString(apiDocsEnabled));

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(jwtSecret);
        jwtProperties.setCookieSecure(cookieSecure);

        ManagementSurfaceProperties managementSurfaceProperties = new ManagementSurfaceProperties();
        managementSurfaceProperties.setPublicApiDocs(publicApiDocs);
        managementSurfaceProperties.setExposePrometheusOnAppPort(exposePrometheusOnAppPort);

        SeedProperties seedProperties = new SeedProperties();
        seedProperties.setEnabled(seedEnabled);

        return new StartupSafetyValidator(environment, jwtProperties, managementSurfaceProperties, seedProperties);
    }
}
