package org.websoso.WSSServer.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.websoso.WSSServer.auth.jwt.CustomAccessDeniedHandler;
import org.websoso.WSSServer.auth.jwt.CustomJwtAuthenticationEntryPoint;
import org.websoso.WSSServer.auth.jwt.JwtAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomJwtAuthenticationEntryPoint customJwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private static final String[] permitAllPaths = {
            "/users/login",
            "/actuator/health",
            "/novels",
            "/novels/autocomplete",
            "/novels/{novelId}",
            "/novels/{novelId}/info",
            "/novels/{novelId}/feeds",
            "/soso-picks",
            "/novels/popular",
            "/feeds",
            "/feeds/popular",
            "/users/{userId}/feeds",
            "/users/profile/{userId}",
            "/{userId}/preferences/genres",
            "/reissue",
            "/login/callback",
            "/login/apple",
            "/auth/login/kakao",
            "/auth/login/apple",
            "/auth/apple/sync",
            "/minimum-version",
            "/keywords/popular",
    };

    /**
     * 인증을 선택으로 두는 조회 API. 토큰이 있으면 그대로 인증에 사용하고, 없으면 비로그인 조회로 처리한다.
     * <p>
     * 메서드까지 지정해 같은 경로의 쓰기 요청이 함께 열리지 않게 한다. 예를 들어 컬렉션 상세는 공유 링크를 위해
     * 비로그인 조회를 허용하지만, 같은 경로의 수정·삭제는 그대로 인증을 요구해야 한다.
     */
    private static final String[] permitAllGetPaths = {
            "/collections/{collectionId}",
    };

    private static final String[] swaggerPaths = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(customJwtAuthenticationEntryPoint);
                    exception.accessDeniedHandler(customAccessDeniedHandler);
                })
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(permitAllPaths).permitAll();
                    auth.requestMatchers(HttpMethod.GET, permitAllGetPaths).permitAll();
                    auth.requestMatchers(swaggerPaths).permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedOriginPatterns("*")
                        .allowedMethods("*");
            }
        };
    }
}
