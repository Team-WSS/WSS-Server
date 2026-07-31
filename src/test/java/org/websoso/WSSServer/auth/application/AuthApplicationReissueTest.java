package org.websoso.WSSServer.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.auth.client.AppleClient;
import org.websoso.WSSServer.auth.client.AppleIdTokenVerifier;
import org.websoso.WSSServer.auth.client.AppleKeyGenerator;
import org.websoso.WSSServer.auth.client.KakaoClient;
import org.websoso.WSSServer.auth.controller.dto.ReissueResponse;
import org.websoso.WSSServer.auth.domain.RefreshToken;
import org.websoso.WSSServer.auth.jwt.JWTUtil;
import org.websoso.WSSServer.auth.jwt.JwtKeyProvider;
import org.websoso.WSSServer.auth.jwt.JwtProvider;
import org.websoso.WSSServer.auth.jwt.TestTokenFactory;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.auth.service.TokenService;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
import org.websoso.WSSServer.notification.repository.UserDeviceRepository;
import org.websoso.WSSServer.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthApplicationReissueTest {

    private static final Long USER_ID = 42L;

    private final TestTokenFactory testTokenFactory = new TestTokenFactory();
    private final JwtKeyProvider jwtKeyProvider = new JwtKeyProvider(TestTokenFactory.TEST_SECRET);
    private final JwtProvider jwtProvider = new JwtProvider(jwtKeyProvider,
            TestTokenFactory.ACCESS_TOKEN_EXPIRATION, TestTokenFactory.REFRESH_TOKEN_EXPIRATION);
    private final JWTUtil jwtUtil = new JWTUtil(jwtKeyProvider);

    @Mock
    private TokenService tokenService;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private UserService userService;

    @Mock
    private KakaoClient kakaoClient;

    @Mock
    private AppleService appleService;

    @Mock
    private AppleClient appleClient;

    @Mock
    private AppleKeyGenerator appleKeyGenerator;

    @Mock
    private AppleIdTokenVerifier appleIdTokenVerifier;

    private AuthApplication authApplication;

    @BeforeEach
    void setUp() {
        authApplication = new AuthApplication(tokenService, jwtProvider, jwtUtil, userDeviceRepository,
                userService, kakaoClient, appleService, appleClient, appleKeyGenerator, appleIdTokenVerifier);
    }

    @DisplayName("유효한 리프레시 토큰이면 Access/Refresh Token을 모두 재발급하고 기존 토큰을 회전한다")
    @Test
    void reissue_validRefreshToken_rotatesAndReturnsNewTokens() {
        String oldRefreshToken = testTokenFactory.createRefreshToken(USER_ID);
        RefreshToken storedRefreshToken = new RefreshToken(oldRefreshToken, USER_ID);
        given(tokenService.findRefreshTokenOrThrow(oldRefreshToken)).willReturn(storedRefreshToken);

        ReissueResponse response = authApplication.reissue(oldRefreshToken);

        assertThat(response.Authorization()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(jwtUtil.getUserIdFromJwt(response.refreshToken())).isEqualTo(USER_ID);

        ArgumentCaptor<String> newRefreshTokenCaptor = ArgumentCaptor.forClass(String.class);
        then(tokenService).should().rotateRefreshToken(eq(storedRefreshToken), newRefreshTokenCaptor.capture(),
                eq(USER_ID));
        assertThat(newRefreshTokenCaptor.getValue()).isEqualTo(response.refreshToken());
    }

    @DisplayName("재발급된 리프레시 토큰은 요청에 사용한 기존 리프레시 토큰과 다른 값이다")
    @Test
    void reissue_returnsRefreshTokenDifferentFromOldOne() {
        String oldRefreshToken = testTokenFactory.createRefreshToken(USER_ID);
        given(tokenService.findRefreshTokenOrThrow(oldRefreshToken))
                .willReturn(new RefreshToken(oldRefreshToken, USER_ID));

        ReissueResponse response = authApplication.reissue(oldRefreshToken);

        assertThat(response.refreshToken()).isNotEqualTo(oldRefreshToken);
    }

    @DisplayName("만료된 리프레시 토큰이면 재발급을 거부한다")
    @Test
    void reissue_expiredRefreshToken_throwsInvalidToken() {
        String expiredRefreshToken = testTokenFactory.createExpiredRefreshToken(USER_ID);

        assertThatThrownBy(() -> authApplication.reissue(expiredRefreshToken))
                .isInstanceOf(CustomAuthException.class)
                .extracting(throwable -> ((CustomAuthException) throwable).getICustomError())
                .isEqualTo(INVALID_TOKEN);

        then(tokenService).shouldHaveNoInteractions();
    }

    @DisplayName("변조된(서명이 다른) 리프레시 토큰이면 재발급을 거부한다")
    @Test
    void reissue_tamperedRefreshToken_throwsInvalidToken() {
        String tamperedRefreshToken = testTokenFactory.createRefreshTokenWithInvalidSignature(USER_ID);

        assertThatThrownBy(() -> authApplication.reissue(tamperedRefreshToken))
                .isInstanceOf(CustomAuthException.class)
                .extracting(throwable -> ((CustomAuthException) throwable).getICustomError())
                .isEqualTo(INVALID_TOKEN);

        then(tokenService).shouldHaveNoInteractions();
    }

    @DisplayName("Access Token으로 재발급을 시도하면 잘못된 토큰 유형으로 거부한다")
    @Test
    void reissue_accessTokenGiven_throwsInvalidToken() {
        String accessToken = testTokenFactory.createAccessToken(USER_ID);

        assertThatThrownBy(() -> authApplication.reissue(accessToken))
                .isInstanceOf(CustomAuthException.class)
                .extracting(throwable -> ((CustomAuthException) throwable).getICustomError())
                .isEqualTo(INVALID_TOKEN);

        then(tokenService).shouldHaveNoInteractions();
    }

    @DisplayName("서명은 유효하지만 저장되지 않은 리프레시 토큰이면 재발급을 거부한다")
    @Test
    void reissue_unstoredRefreshToken_throwsInvalidToken() {
        String refreshToken = testTokenFactory.createRefreshToken(USER_ID);
        given(tokenService.findRefreshTokenOrThrow(refreshToken))
                .willThrow(new CustomAuthException(INVALID_TOKEN, "given token is invalid token for reissue"));

        assertThatThrownBy(() -> authApplication.reissue(refreshToken))
                .isInstanceOf(CustomAuthException.class)
                .extracting(throwable -> ((CustomAuthException) throwable).getICustomError())
                .isEqualTo(INVALID_TOKEN);

        then(tokenService).should(never())
                .rotateRefreshToken(any(), any(), any());
    }
}
