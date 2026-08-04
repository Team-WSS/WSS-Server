package org.websoso.WSSServer.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.TOKEN_REQUEST_FAILED;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.auth.client.KakaoClient;
import org.websoso.WSSServer.auth.controller.dto.AuthResponse;
import org.websoso.WSSServer.auth.jwt.JWTUtil;
import org.websoso.WSSServer.auth.jwt.JwtProvider;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.auth.service.TokenService;
import org.websoso.WSSServer.auth.service.dto.AppleAuthResult;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;
import org.websoso.WSSServer.notification.service.UserDeviceService;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthApplicationAppleLoginTest {

    private static final String AUTHORIZATION_CODE = "apple-authorization-code";
    private static final String ID_TOKEN = "apple-id-token";
    private static final String APPLE_REFRESH_TOKEN = "apple-refresh-token";
    private static final String USER_IDENTIFIER = "001234.abcdefghijklmn.1234";
    private static final String EMAIL = "websoso@websoso.org";
    private static final String EXPECTED_SOCIAL_ID = "apple_" + USER_IDENTIFIER;
    private static final String EXPECTED_DEFAULT_NICKNAME = "a*" + "abcdefgh";
    private static final String ACCESS_TOKEN = "access-token-value";
    private static final String REFRESH_TOKEN = "refresh-token-value";

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private UserDeviceService userDeviceService;

    @Mock
    private UserService userService;

    @Mock
    private KakaoClient kakaoClient;

    @Mock
    private AppleService appleService;

    @Mock
    private User user;

    private AuthApplication authApplication;

    @BeforeEach
    void setUp() {
        authApplication = new AuthApplication(tokenService, jwtProvider, jwtUtil, userDeviceService,
                userService, kakaoClient, appleService);
    }

    @DisplayName("애플 로그인에 성공하면 Apple 인증 결과로 유저를 조회·생성하고 토큰을 발급한다")
    @Test
    void loginApple_success() {
        given(appleService.authenticate(AUTHORIZATION_CODE, ID_TOKEN))
                .willReturn(AppleAuthResult.of(USER_IDENTIFIER, EMAIL, APPLE_REFRESH_TOKEN));
        given(userService.getOrCreateAppleUser(EXPECTED_SOCIAL_ID, EMAIL, EXPECTED_DEFAULT_NICKNAME))
                .willReturn(user);
        given(jwtProvider.generateAccessToken(any())).willReturn(ACCESS_TOKEN);
        given(jwtProvider.generateRefreshToken(any())).willReturn(REFRESH_TOKEN);
        given(user.isTemporaryNickname()).willReturn(false);

        AuthResponse response = authApplication.loginApple(AUTHORIZATION_CODE, ID_TOKEN);

        assertThat(response.Authorization()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.isRegister()).isTrue();
        then(appleService).should().upsertRefreshToken(user, APPLE_REFRESH_TOKEN);
        then(tokenService).should().saveRefreshToken(user, REFRESH_TOKEN);
    }

    @DisplayName("Apple 인증이 실패하면 유저 처리와 토큰 저장을 수행하지 않고 예외를 전파한다")
    @Test
    void loginApple_authenticationFailed() {
        given(appleService.authenticate(AUTHORIZATION_CODE, ID_TOKEN))
                .willThrow(new CustomAppleLoginException(TOKEN_REQUEST_FAILED, "apple token request failed"));

        assertThatThrownBy(() -> authApplication.loginApple(AUTHORIZATION_CODE, ID_TOKEN))
                .isInstanceOf(CustomAppleLoginException.class)
                .extracting(throwable -> ((CustomAppleLoginException) throwable).getICustomError())
                .isEqualTo(TOKEN_REQUEST_FAILED);

        then(userService).should(never())
                .getOrCreateAppleUser(EXPECTED_SOCIAL_ID, EMAIL, EXPECTED_DEFAULT_NICKNAME);
        then(tokenService).should(never()).saveRefreshToken(user, REFRESH_TOKEN);
    }
}
