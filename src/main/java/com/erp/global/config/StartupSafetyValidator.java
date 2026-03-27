package com.erp.global.config;

import com.erp.global.security.ManagementSurfaceProperties;
import com.erp.global.security.jwt.JwtProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class StartupSafetyValidator {

    private static final String LEGACY_JWT_FALLBACK_SECRET =
            "your-256-bit-secret-key-here-must-be-at-least-32-characters";
    private static final Set<String> ALLOWED_RUNTIME_PROFILES = Set.of("local", "demo", "prod", "test");

    private final Environment environment;
    private final JwtProperties jwtProperties;
    private final ManagementSurfaceProperties managementSurfaceProperties;
    private final SeedProperties seedProperties;

    @PostConstruct
    void validate() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            throw new IllegalStateException(
                    "No active Spring profile configured. Use --spring.profiles.active=local|demo|prod."
            );
        }

        boolean hasKnownRuntimeProfile = Arrays.stream(activeProfiles).anyMatch(ALLOWED_RUNTIME_PROFILES::contains);
        if (!hasKnownRuntimeProfile) {
            throw new IllegalStateException(
                    "Unsupported runtime profile. Expected one of local, demo, prod, test."
            );
        }

        if (isActive("prod")) {
            validateProdSafety();
        }
    }

    private void validateProdSafety() {
        String jwtSecret = jwtProperties.getSecret();
        if (jwtSecret == null || jwtSecret.isBlank() || LEGACY_JWT_FALLBACK_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException("Production profile requires a real JWT_SECRET.");
        }

        if (!jwtProperties.isCookieSecure()) {
            throw new IllegalStateException("Production profile must keep jwt.cookie-secure=true.");
        }

        if (seedProperties.isEnabled()) {
            throw new IllegalStateException("Production profile must keep app.seed.enabled=false.");
        }

        if (managementSurfaceProperties.isPublicApiDocs()) {
            throw new IllegalStateException("Production profile must not expose Swagger/OpenAPI publicly.");
        }

        if (managementSurfaceProperties.isExposePrometheusOnAppPort()) {
            throw new IllegalStateException("Production profile must not expose Prometheus on the app port.");
        }

        if (environment.getProperty("springdoc.api-docs.enabled", Boolean.class, false)) {
            throw new IllegalStateException("Production profile must keep springdoc.api-docs.enabled=false.");
        }

        if (environment.getProperty("springdoc.swagger-ui.enabled", Boolean.class, false)) {
            throw new IllegalStateException("Production profile must keep springdoc.swagger-ui.enabled=false.");
        }
    }

    private boolean isActive(String profile) {
        return Arrays.asList(environment.getActiveProfiles()).contains(profile);
    }
}
