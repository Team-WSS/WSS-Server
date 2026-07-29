package org.websoso.WSSServer.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * TestTokenFactory가 발급한 실제 형식의 토큰이 실제(mock 아닌) JWTUtil/JwtProvider를 거쳐
 * JwtAuthenticationFilter를 그대로 통과하는지 확인하는 end-to-end 테스트.
 * Spring 컨텍스트/DB/Redis 없이 순수 자바 객체 조합만으로 동작한다 (issue #556 완료조건:
 * "발급한 테스트 Access Token을 JWT 인증 필터에 적용할 수 있습니다").
 */
class JwtAuthenticationFilterRealTokenTest {

    private static final Long USER_ID = 42L;

    private final TestTokenFactory testTokenFactory = new TestTokenFactory();
    private final JWTUtil jwtUtil = new JWTUtil(new JwtKeyProvider(TestTokenFactory.TEST_SECRET));
    private final FilterChain filterChain = mock(FilterChain.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, new ObjectMapper());

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("실제로 발급한 Access Token은 실제 필터를 통과해 SecurityContext에 인증 정보를 남긴다")
    @Test
    void realAccessToken_passesThroughRealFilter() throws Exception {
        String token = testTokenFactory.createAccessToken(USER_ID);

        MockHttpServletResponse response = doFilter(bearerRequest(token));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getPrincipal()).isEqualTo(USER_ID);
        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(any(), any());
    }

    @DisplayName("실제로 발급한 만료 Access Token은 실제 필터에서 401과 AUTH-000으로 차단된다")
    @Test
    void realExpiredAccessToken_blockedByRealFilter() throws Exception {
        String token = testTokenFactory.createExpiredAccessToken(USER_ID);

        MockHttpServletResponse response = doFilter(bearerRequest(token));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH-000");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @DisplayName("실제로 발급한 Refresh Token으로 인증을 시도하면 실제 필터에서 401과 AUTH-003으로 차단된다")
    @Test
    void realRefreshTokenUsedAsAccess_blockedByRealFilter() throws Exception {
        String token = testTokenFactory.createRefreshToken(USER_ID);

        MockHttpServletResponse response = doFilter(bearerRequest(token));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH-003");
    }

    @DisplayName("변조된 토큰은 실제 필터에서 401과 AUTH-001로 차단된다")
    @Test
    void tamperedToken_blockedByRealFilter() throws Exception {
        String token = testTokenFactory.createAccessToken(USER_ID) + "tampered";

        MockHttpServletResponse response = doFilter(bearerRequest(token));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH-001");
    }

    private MockHttpServletRequest bearerRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private MockHttpServletResponse doFilter(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, filterChain);
        return response;
    }
}
