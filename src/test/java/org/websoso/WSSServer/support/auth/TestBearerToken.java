package org.websoso.WSSServer.support.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.websoso.WSSServer.auth.jwt.TestTokenFactory;

/**
 * MockMvc 요청에 {@link TestTokenFactory}가 발급한 실제 형식의 토큰을
 * {@code Authorization: Bearer ...} 헤더로 적용하는 공통 도구.
 *
 * <pre>
 * mockMvc.perform(post("/auth/logout").with(TestBearerToken.accessToken(USER_ID)))
 * </pre>
 */
public final class TestBearerToken {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final TestTokenFactory TOKEN_FACTORY = new TestTokenFactory();

    private TestBearerToken() {
    }

    public static RequestPostProcessor accessToken(Long userId) {
        return bearer(TOKEN_FACTORY.createAccessToken(userId));
    }

    public static RequestPostProcessor refreshToken(Long userId) {
        return bearer(TOKEN_FACTORY.createRefreshToken(userId));
    }

    public static RequestPostProcessor expiredAccessToken(Long userId) {
        return bearer(TOKEN_FACTORY.createExpiredAccessToken(userId));
    }

    public static RequestPostProcessor tamperedAccessToken(Long userId) {
        return bearer(TOKEN_FACTORY.createAccessToken(userId) + "tampered");
    }

    public static RequestPostProcessor bearer(String token) {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + token);
            return request;
        };
    }
}
