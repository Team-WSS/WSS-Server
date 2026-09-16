package org.websoso.WSSServer.support.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.websoso.WSSServer.auth.jwt.JWTUtil;
import org.websoso.WSSServer.auth.jwt.JwtKeyProvider;
import org.websoso.WSSServer.auth.jwt.TestTokenFactory;

/**
 * Controller 테스트에서 실제 JWT 검증 구성을 사용하기 위한 테스트 전용 구성.
 * 프로덕션 {@code jwt.secret} 프로퍼티 대신 {@link TestTokenFactory#TEST_SECRET}으로
 * {@link JwtKeyProvider}를 만들어, application-*.yml 없이도 실제 {@link JWTUtil}과
 * {@code JwtAuthenticationFilter}가 동작하게 한다.
 */
@TestConfiguration
public class ControllerAuthTestConfig {

    @Bean
    public JwtKeyProvider jwtKeyProvider() {
        return new JwtKeyProvider(TestTokenFactory.TEST_SECRET);
    }

    @Bean
    public JWTUtil jwtUtil(JwtKeyProvider jwtKeyProvider) {
        return new JWTUtil(jwtKeyProvider);
    }
}
