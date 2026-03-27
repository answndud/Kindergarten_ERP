package com.erp.global.config;

import com.erp.domain.auth.service.AuthSessionRegistryService;
import com.erp.global.security.ClientIpResolver;
import com.erp.global.security.CustomAuthenticationEntryPoint;
import com.erp.global.security.ManagementSurfaceProperties;
import com.erp.global.security.jwt.JwtFilter;
import com.erp.global.security.jwt.JwtTokenProvider;
import com.erp.global.security.oauth2.CustomOAuth2UserService;
import com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.erp.global.security.user.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final AuthSessionRegistryService authSessionRegistryService;
    private final ClientIpResolver clientIpResolver;
    private final ManagementSurfaceProperties managementSurfaceProperties;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          CustomUserDetailsService userDetailsService,
                          AuthSessionRegistryService authSessionRegistryService,
                          ClientIpResolver clientIpResolver,
                          ManagementSurfaceProperties managementSurfaceProperties,
                          CustomAuthenticationEntryPoint authenticationEntryPoint,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.authSessionRegistryService = authSessionRegistryService;
        this.clientIpResolver = clientIpResolver;
        this.managementSurfaceProperties = managementSurfaceProperties;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    }

    /**
     * 비밀번호 인코더 (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 매니저
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * JWT 필터
     */
    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtTokenProvider, userDetailsService, authSessionRegistryService, clientIpResolver);
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:8080"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Security 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new CsrfTokenRequestAttributeHandler();
        String[] publicEndpoints = buildPublicEndpoints();

        http
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF 보호 (쿠키 기반 JWT 사용)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/logout")
                        .csrfTokenRequestHandler(csrfTokenRequestHandler)
                )

                // 폼 로그인 비활성화 (JWT 사용)
                .formLogin(form -> form.disable())

                // HTTP Basic 비활성화
                .httpBasic(basic -> basic.disable())

                // 세션 관리 정책: OAuth2 핸드셰이크를 위해 필요한 경우만 세션 사용
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> {
                    auth
                            // 공개 경로
                            .requestMatchers(publicEndpoints).permitAll();

                    if (!managementSurfaceProperties.isPublicApiDocs()) {
                        auth.requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).hasRole("PRINCIPAL");
                    }

                    auth
                            // 관리자 전용
                            .requestMatchers("/main/admin").hasAnyRole(
                                    "PRINCIPAL",
                                    "TEACHER"
                            )

                            // 사용자 전용
                            .requestMatchers("/main/user").hasRole("PARENT")

                            // 그 외 요청은 인증 필요
                            .anyRequest().authenticated();
                })

                // 인증 예외 처리 (로그인 페이지로 리다이렉트)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies(
                                jwtTokenProvider.getAccessTokenCookieName(),
                                jwtTokenProvider.getRefreshTokenCookieName()
                        )
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler((request, response, exception) ->
                                response.sendRedirect("/login?error=social_login_failed"))
                )

                // JWT 필터 추가 (UsernamePasswordAuthenticationFilter 앞에)
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }

    private String[] buildPublicEndpoints() {
        List<String> publicEndpoints = new ArrayList<>(List.of(
                "/",
                "/login",
                "/signup",
                "/oauth2/**",
                "/login/oauth2/**",
                "/actuator/health",
                "/actuator/health/**",
                "/actuator/info",
                "/api/v1/auth/signup",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/kindergarten/create",
                "/kindergarten/select",
                "/css/**",
                "/js/**",
                "/img/**",
                "/images/**",
                "/favicon.ico",
                "/error",
                "/.well-known/**"
        ));

        if (managementSurfaceProperties.isPublicApiDocs()) {
            publicEndpoints.add("/swagger-ui.html");
            publicEndpoints.add("/swagger-ui/**");
            publicEndpoints.add("/v3/api-docs");
            publicEndpoints.add("/v3/api-docs/**");
        }

        if (managementSurfaceProperties.isExposePrometheusOnAppPort()) {
            publicEndpoints.add("/actuator/prometheus");
            publicEndpoints.add("/actuator/prometheus/**");
        }

        return publicEndpoints.toArray(String[]::new);
    }
}
