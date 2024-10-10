package org.websoso.WSSServer.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.websoso.WSSServer.config.jwt.CustomAccessDeniedHandler;
import org.websoso.WSSServer.config.jwt.CustomJwtAuthenticationEntryPoint;
import org.websoso.WSSServer.config.jwt.JwtAuthenticationFilter;
import org.websoso.WSSServer.oauth2.CustomAuthenticationSuccessHandler;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomJwtAuthenticationEntryPoint customJwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final DefaultOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    private final String[] permitAllPaths = {
            "/users/login",
            "/actuator/health",
            "/novels",
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
            "/login/apple"
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
                    auth.anyRequest().authenticated();
                })
                .oauth2Login(oauth2 -> {
                    oauth2.redirectionEndpoint(endpoint -> endpoint.baseUri("/oauth2/callback/*"));
                    oauth2.userInfoEndpoint(endpoint -> endpoint.userService(customOAuth2UserService));
                    oauth2.successHandler(customAuthenticationSuccessHandler);
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
