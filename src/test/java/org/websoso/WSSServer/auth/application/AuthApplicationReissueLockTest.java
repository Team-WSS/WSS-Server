package org.websoso.WSSServer.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.auth.client.KakaoClient;
import org.websoso.WSSServer.auth.domain.RefreshToken;
import org.websoso.WSSServer.auth.jwt.JWTUtil;
import org.websoso.WSSServer.auth.jwt.JwtKeyProvider;
import org.websoso.WSSServer.auth.jwt.JwtProvider;
import org.websoso.WSSServer.auth.jwt.TestTokenFactory;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.auth.service.RefreshTokenLockService;
import org.websoso.WSSServer.auth.service.TokenService;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
import org.websoso.WSSServer.notification.service.UserDeviceService;
import org.websoso.WSSServer.user.service.UserService;

/**
 * 재발급 유스케이스가 잠금을 어떤 순서로 사용하는지 검증한다.
 * 잠금 자체의 동시성 동작은 {@link AuthApplicationReissueConcurrencyTest}에서 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class AuthApplicationReissueLockTest {

    private static final Long USER_ID = 42L;

    private final TestTokenFactory testTokenFactory = new TestTokenFactory();
    private final JwtKeyProvider jwtKeyProvider = new JwtKeyProvider(TestTokenFactory.TEST_SECRET);
    private final JwtProvider jwtProvider = new JwtProvider(jwtKeyProvider,
            TestTokenFactory.ACCESS_TOKEN_EXPIRATION, TestTokenFactory.REFRESH_TOKEN_EXPIRATION);
    private final JWTUtil jwtUtil = new JWTUtil(jwtKeyProvider);

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenLockService refreshTokenLockService;

    @Mock
    private UserDeviceService userDeviceService;

    @Mock
    private UserService userService;

    @Mock
    private KakaoClient kakaoClient;

    @Mock
    private AppleService appleService;

    private AuthApplication authApplication;

    @BeforeEach
    void setUp() {
        authApplication = new AuthApplication(tokenService, refreshTokenLockService, jwtProvider, jwtUtil,
                userDeviceService, userService, kakaoClient, appleService);
    }

    @DisplayName("잠금을 획득하지 못하면 저장된 토큰을 조회하지 않고 기존 INVALID_TOKEN 계약으로 거부한다")
    @Test
    void reissue_whenLockNotAcquired_throwsInvalidTokenWithoutRotating() {
        String refreshToken = testTokenFactory.createRefreshToken(USER_ID);
        given(refreshTokenLockService.tryLock(eq(refreshToken), anyString())).willReturn(false);

        assertThatThrownBy(() -> authApplication.reissue(refreshToken))
                .isInstanceOf(CustomAuthException.class)
                .extracting(throwable -> ((CustomAuthException) throwable).getICustomError())
                .isEqualTo(INVALID_TOKEN);

        then(tokenService).shouldHaveNoInteractions();
        then(refreshTokenLockService).should(never()).unlock(anyString(), anyString());
    }

    @DisplayName("잠금을 획득한 뒤에 저장된 토큰 존재 여부를 다시 확인하고 회전한다")
    @Test
    void reissue_verifiesStoredTokenAfterAcquiringLock() {
        String refreshToken = testTokenFactory.createRefreshToken(USER_ID);
        given(refreshTokenLockService.tryLock(eq(refreshToken), anyString())).willReturn(true);
        given(tokenService.findRefreshTokenOrThrow(refreshToken))
                .willReturn(new RefreshToken(refreshToken, USER_ID));

        authApplication.reissue(refreshToken);

        InOrder inOrder = Mockito.inOrder(refreshTokenLockService, tokenService);
        inOrder.verify(refreshTokenLockService).tryLock(eq(refreshToken), anyString());
        inOrder.verify(tokenService).findRefreshTokenOrThrow(refreshToken);
        inOrder.verify(tokenService).rotateRefreshToken(Mockito.any(), anyString(), eq(USER_ID));
        inOrder.verify(refreshTokenLockService).unlock(eq(refreshToken), anyString());
    }

    @DisplayName("잠금은 획득할 때 사용한 요청별 소유자 값으로 해제한다")
    @Test
    void reissue_releasesLockWithTheSameRequestOwner() {
        String refreshToken = testTokenFactory.createRefreshToken(USER_ID);
        ArgumentCaptor<String> lockOwnerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unlockOwnerCaptor = ArgumentCaptor.forClass(String.class);
        given(refreshTokenLockService.tryLock(eq(refreshToken), anyString())).willReturn(true);
        given(tokenService.findRefreshTokenOrThrow(refreshToken))
                .willReturn(new RefreshToken(refreshToken, USER_ID));

        authApplication.reissue(refreshToken);

        then(refreshTokenLockService).should().tryLock(eq(refreshToken), lockOwnerCaptor.capture());
        then(refreshTokenLockService).should().unlock(eq(refreshToken), unlockOwnerCaptor.capture());
        assertThat(unlockOwnerCaptor.getValue()).isEqualTo(lockOwnerCaptor.getValue());
    }

    @DisplayName("요청마다 서로 다른 잠금 소유자 값을 사용한다")
    @Test
    void reissue_usesDifferentLockOwnerPerRequest() {
        String refreshToken = testTokenFactory.createRefreshToken(USER_ID);
        ArgumentCaptor<String> lockOwnerCaptor = ArgumentCaptor.forClass(String.class);
        given(refreshTokenLockService.tryLock(eq(refreshToken), anyString())).willReturn(true);
        given(tokenService.findRefreshTokenOrThrow(refreshToken))
                .willReturn(new RefreshToken(refreshToken, USER_ID));

        authApplication.reissue(refreshToken);
        authApplication.reissue(refreshToken);

        then(refreshTokenLockService).should(Mockito.times(2)).tryLock(eq(refreshToken), lockOwnerCaptor.capture());
        assertThat(lockOwnerCaptor.getAllValues()).doesNotHaveDuplicates();
    }

    @DisplayName("회전 도중 예외가 발생해도 잠금을 해제한다")
    @Test
    void reissue_whenRotationFails_stillReleasesLock() {
        String refreshToken = testTokenFactory.createRefreshToken(USER_ID);
        given(refreshTokenLockService.tryLock(eq(refreshToken), anyString())).willReturn(true);
        given(tokenService.findRefreshTokenOrThrow(refreshToken))
                .willThrow(new CustomAuthException(INVALID_TOKEN, "given token is invalid token for reissue"));

        assertThatThrownBy(() -> authApplication.reissue(refreshToken))
                .isInstanceOf(CustomAuthException.class);

        then(refreshTokenLockService).should().unlock(eq(refreshToken), anyString());
    }

    @DisplayName("서명 검증에 실패한 토큰은 잠금을 시도하지 않는다")
    @Test
    void reissue_whenTokenIsInvalid_doesNotTouchLock() {
        String expiredRefreshToken = testTokenFactory.createExpiredRefreshToken(USER_ID);

        assertThatThrownBy(() -> authApplication.reissue(expiredRefreshToken))
                .isInstanceOf(CustomAuthException.class);

        then(refreshTokenLockService).shouldHaveNoInteractions();
    }
}
