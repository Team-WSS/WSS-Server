package org.websoso.WSSServer.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.exception.error.CustomKakaoError.INVALID_KAKAO_ACCESS_TOKEN;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.auth.client.KakaoClient;
import org.websoso.WSSServer.auth.client.dto.KakaoUserInfo;
import org.websoso.WSSServer.auth.controller.dto.AuthResponse;
import org.websoso.WSSServer.auth.jwt.CustomAuthenticationToken;
import org.websoso.WSSServer.auth.jwt.JWTUtil;
import org.websoso.WSSServer.auth.jwt.JwtProvider;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.auth.service.TokenService;
import org.websoso.WSSServer.exception.exception.CustomKakaoException;
import org.websoso.WSSServer.notification.service.UserDeviceService;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthApplicationKakaoLoginTest {

    private static final String KAKAO_ACCESS_TOKEN = "kakao-access-token";
    private static final Long USER_ID = 42L;
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

    @Captor
    private ArgumentCaptor<CustomAuthenticationToken> authenticationTokenCaptor;

    private AuthApplication authApplication;

    @BeforeEach
    void setUp() {
        authApplication = new AuthApplication(tokenService, jwtProvider, jwtUtil, userDeviceService,
                userService, kakaoClient, appleService);
    }

    @DisplayName("이미 닉네임을 등록한 기존 사용자가 카카오 로그인하면 isRegister가 true인 토큰 쌍을 발급한다")
    @Test
    void loginKakao_registeredUser_returnsIsRegisterTrue() {
        KakaoUserInfo kakaoUserInfo = kakaoUserInfo();
        given(kakaoClient.getUserInfo(KAKAO_ACCESS_TOKEN)).willReturn(kakaoUserInfo);
        given(userService.getOrCreateKakaoUser(kakaoUserInfo)).willReturn(user);
        given(jwtProvider.generateAccessToken(any())).willReturn(ACCESS_TOKEN);
        given(jwtProvider.generateRefreshToken(any())).willReturn(REFRESH_TOKEN);
        given(user.isTemporaryNickname()).willReturn(false);

        AuthResponse response = authApplication.loginKakao(KAKAO_ACCESS_TOKEN);

        assertThat(response.Authorization()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.isRegister()).isTrue();
        then(tokenService).should().saveRefreshToken(user, REFRESH_TOKEN);
    }

    @DisplayName("임시 닉네임인 신규 사용자가 카카오 로그인하면 isRegister가 false인 토큰 쌍을 발급한다")
    @Test
    void loginKakao_newUserWithTemporaryNickname_returnsIsRegisterFalse() {
        KakaoUserInfo kakaoUserInfo = kakaoUserInfo();
        given(kakaoClient.getUserInfo(KAKAO_ACCESS_TOKEN)).willReturn(kakaoUserInfo);
        given(userService.getOrCreateKakaoUser(kakaoUserInfo)).willReturn(user);
        given(jwtProvider.generateAccessToken(any())).willReturn(ACCESS_TOKEN);
        given(jwtProvider.generateRefreshToken(any())).willReturn(REFRESH_TOKEN);
        given(user.isTemporaryNickname()).willReturn(true);

        AuthResponse response = authApplication.loginKakao(KAKAO_ACCESS_TOKEN);

        assertThat(response.isRegister()).isFalse();
        then(tokenService).should().saveRefreshToken(user, REFRESH_TOKEN);
    }

    @DisplayName("카카오 로그인은 조회하거나 생성한 사용자의 식별자로 Access / Refresh Token을 발급한다")
    @Test
    void loginKakao_issuesTokenPairForResolvedUser() {
        KakaoUserInfo kakaoUserInfo = kakaoUserInfo();
        given(kakaoClient.getUserInfo(KAKAO_ACCESS_TOKEN)).willReturn(kakaoUserInfo);
        given(userService.getOrCreateKakaoUser(kakaoUserInfo)).willReturn(user);
        given(user.getUserId()).willReturn(USER_ID);
        given(jwtProvider.generateAccessToken(any())).willReturn(ACCESS_TOKEN);
        given(jwtProvider.generateRefreshToken(any())).willReturn(REFRESH_TOKEN);

        authApplication.loginKakao(KAKAO_ACCESS_TOKEN);

        then(jwtProvider).should().generateAccessToken(authenticationTokenCaptor.capture());
        then(jwtProvider).should().generateRefreshToken(authenticationTokenCaptor.capture());
        assertThat(authenticationTokenCaptor.getAllValues())
                .extracting(CustomAuthenticationToken::getPrincipal)
                .containsExactly(USER_ID, USER_ID);
    }

    @DisplayName("카카오 사용자 정보 조회가 실패하면 사용자 조회·생성과 토큰 저장을 수행하지 않고 예외를 전파한다")
    @Test
    void loginKakao_userInfoRequestFailed() {
        given(kakaoClient.getUserInfo(KAKAO_ACCESS_TOKEN))
                .willThrow(new CustomKakaoException(INVALID_KAKAO_ACCESS_TOKEN, "invalid kakao access token"));

        assertThatThrownBy(() -> authApplication.loginKakao(KAKAO_ACCESS_TOKEN))
                .isInstanceOf(CustomKakaoException.class)
                .extracting(throwable -> ((CustomKakaoException) throwable).getICustomError())
                .isEqualTo(INVALID_KAKAO_ACCESS_TOKEN);

        then(userService).shouldHaveNoInteractions();
        then(jwtProvider).shouldHaveNoInteractions();
        then(tokenService).should(never()).saveRefreshToken(any(), any());
    }

    private KakaoUserInfo kakaoUserInfo() {
        return new KakaoUserInfo(
                1234567890L,
                new KakaoUserInfo.Properties("websoso"),
                new KakaoUserInfo.KakaoAccount("websoso@websoso.org")
        );
    }
}
