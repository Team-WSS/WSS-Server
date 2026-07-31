package org.websoso.WSSServer.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private static final String TOKEN = "dummy-token";

    private final JWTUtil jwtUtil = mock(JWTUtil.class);
    private final FilterChain filterChain = mock(FilterChain.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, new ObjectMapper());

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("유효한 Access Token이면 인증 정보를 설정하고 필터 체인을 계속 진행한다")
    @Test
    void validAccessToken_authenticatesAndContinues() throws Exception {
        given(jwtUtil.validateJWT(TOKEN)).willReturn(JwtValidationType.VALID_ACCESS);
        given(jwtUtil.getUserIdFromJwt(TOKEN)).willReturn(42L);

        MockHttpServletResponse response = doFilter(bearerRequest(TOKEN));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getPrincipal()).isEqualTo(42L);
        verify(filterChain).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @DisplayName("만료된 Access Token이면 401과 AUTH-000을 응답하고 체인을 중단한다")
    @Test
    void expiredAccessToken_returnsAuthTokenExpired() throws Exception {
        given(jwtUtil.validateJWT(TOKEN)).willReturn(JwtValidationType.EXPIRED_ACCESS);

        MockHttpServletResponse response = doFilter(bearerRequest(TOKEN));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH-000");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @DisplayName("Refresh Token으로 인증을 시도하면(VALID_REFRESH) 401과 AUTH-003을 응답한다")
    @Test
    void validRefreshTokenUsedAsAccess_returnsWrongTokenType() throws Exception {
        given(jwtUtil.validateJWT(TOKEN)).willReturn(JwtValidationType.VALID_REFRESH);

        MockHttpServletResponse response = doFilter(bearerRequest(TOKEN));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH-003");
    }

    @DisplayName("만료된 Refresh Token으로 인증을 시도해도 401과 AUTH-003을 응답한다")
    @Test
    void expiredRefreshTokenUsedAsAccess_returnsWrongTokenType() throws Exception {
        given(jwtUtil.validateJWT(TOKEN)).willReturn(JwtValidationType.EXPIRED_REFRESH);

        MockHttpServletResponse response = doFilter(bearerRequest(TOKEN));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH-003");
    }

    @DisplayName("변조/미지원/빈 토큰이면 401과 AUTH-001을 응답한다")
    @Test
    void tamperedOrUnsupportedToken_returnsInvalidToken() throws Exception {
        for (JwtValidationType type : new JwtValidationType[]{
                JwtValidationType.INVALID_TOKEN,
                JwtValidationType.INVALID_SIGNATURE,
                JwtValidationType.UNSUPPORTED_TOKEN,
                JwtValidationType.EMPTY_TOKEN
        }) {
            given(jwtUtil.validateJWT(TOKEN)).willReturn(type);

            MockHttpServletResponse response = doFilter(bearerRequest(TOKEN));

            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("AUTH-001");
        }
    }

    @DisplayName("지원하지 않는 subject 토큰이면 401과 AUTH-001을 응답하고 체인을 중단한다")
    @Test
    void unsupportedSubjectToken_returnsInvalidTokenAndStopsChain() throws Exception {
        given(jwtUtil.validateJWT(TOKEN)).willReturn(JwtValidationType.UNSUPPORTED_SUBJECT);

        MockHttpServletResponse response = doFilter(bearerRequest(TOKEN));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH-001");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @DisplayName("Authorization 헤더가 없으면 익명 인증으로 설정하고 필터 체인을 계속 진행한다")
    @Test
    void noToken_setsAnonymousAuthenticationAndContinues() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        doFilter(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isInstanceOf(AnonymousAuthenticationToken.class);
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
