package org.websoso.WSSServer.auth.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.refreshToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.tamperedAccessToken;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.websoso.WSSServer.application.AccountApplication;
import org.websoso.WSSServer.auth.application.AuthApplication;
import org.websoso.WSSServer.auth.controller.dto.LogoutRequest;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.UserRepository;
import org.websoso.WSSServer.user.service.UserService;

/**
 * 실제 JwtAuthenticationFilter / JWTUtil을 통과하는 인증 Controller 요청 대표 테스트.
 * 인증에 성공하면 토큰의 userId가 CustomUserArgumentResolver를 거쳐
 * {@code @AuthenticationPrincipal User}로 전달되는 흐름까지 검증한다.
 */
@AuthenticatedControllerTest(AuthController.class)
class AuthControllerLogoutAuthenticationTest {

    private static final Long USER_ID = 42L;
    private static final LogoutRequest LOGOUT_REQUEST = new LogoutRequest("refresh-token", "device-identifier");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthApplication authApplication;

    @MockBean
    private AppleService appleService;

    @MockBean
    private AccountApplication accountApplication;

    @MockBean
    private UserRepository userRepository;

    private final User loginUser = mock(User.class);

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(loginUser);
    }

    @DisplayName("유효한 Access Token 요청은 실제 JWT 필터를 통과하고 토큰의 사용자로 로그아웃된다")
    @Test
    void logoutWithValidAccessToken() throws Exception {
        mockMvc.perform(logoutRequest().with(accessToken(USER_ID)))
                .andExpect(status().isNoContent());

        then(userService).should().getUserOrException(USER_ID);
        then(authApplication).should().logout(loginUser, LOGOUT_REQUEST);
    }

    @DisplayName("Authorization 헤더가 없으면 401로 거부된다")
    @Test
    void logoutWithoutAuthorizationHeader() throws Exception {
        mockMvc.perform(logoutRequest())
                .andExpect(status().isUnauthorized());

        then(authApplication).should(never()).logout(any(), any());
    }

    @DisplayName("변조된 Access Token 요청은 401과 INVALID_TOKEN으로 거부된다")
    @Test
    void logoutWithTamperedAccessToken() throws Exception {
        mockMvc.perform(logoutRequest().with(tamperedAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("AUTH-001")));

        then(authApplication).should(never()).logout(any(), any());
    }

    @DisplayName("Refresh Token으로 인증을 시도하면 401과 WRONG_TOKEN_TYPE으로 거부된다")
    @Test
    void logoutWithRefreshToken() throws Exception {
        mockMvc.perform(logoutRequest().with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("AUTH-003")));

        then(authApplication).should(never()).logout(any(), any());
    }

    private MockHttpServletRequestBuilder logoutRequest() throws Exception {
        return post("/auth/logout")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LOGOUT_REQUEST));
    }
}
