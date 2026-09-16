package org.websoso.WSSServer.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.auth.client.KakaoClient;
import org.websoso.WSSServer.auth.controller.dto.ReissueResponse;
import org.websoso.WSSServer.auth.domain.RefreshToken;
import org.websoso.WSSServer.auth.jwt.CustomAuthenticationToken;
import org.websoso.WSSServer.auth.jwt.JWTUtil;
import org.websoso.WSSServer.auth.jwt.JwtKeyProvider;
import org.websoso.WSSServer.auth.jwt.JwtProvider;
import org.websoso.WSSServer.auth.jwt.TestTokenFactory;
import org.websoso.WSSServer.auth.repository.RefreshTokenRepository;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.auth.service.TokenService;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
import org.websoso.WSSServer.notification.service.UserDeviceService;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

/**
 * 저장소를 토큰 문자열 키 기반 맵으로 흉내 내어, 실제 {@link TokenService} 위에서 회전 결과를 상태로 검증한다.
 * {@link RefreshToken}의 @Id가 토큰 문자열이므로 delete/save가 같은 키를 가리키면 회전이 무효화된다.
 */
@ExtendWith(MockitoExtension.class)
class AuthApplicationReissueRotationTest {

    private static final Long USER_ID = 42L;

    private final JwtKeyProvider jwtKeyProvider = new JwtKeyProvider(TestTokenFactory.TEST_SECRET);
    private final JwtProvider jwtProvider = new JwtProvider(jwtKeyProvider,
            TestTokenFactory.ACCESS_TOKEN_EXPIRATION, TestTokenFactory.REFRESH_TOKEN_EXPIRATION);
    private final JWTUtil jwtUtil = new JWTUtil(jwtKeyProvider);
    private final Map<String, RefreshToken> refreshTokenStore = new HashMap<>();

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

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

    private TokenService tokenService;
    private AuthApplication authApplication;

    @BeforeEach
    void setUp() {
        given(user.getUserId()).willReturn(USER_ID);
        given(refreshTokenRepository.findByRefreshToken(anyString()))
                .willAnswer(invocation -> Optional.ofNullable(refreshTokenStore.get(invocation.getArgument(0))));
        given(refreshTokenRepository.save(any(RefreshToken.class)))
                .willAnswer(invocation -> {
                    RefreshToken saved = invocation.getArgument(0);
                    refreshTokenStore.put(saved.getRefreshToken(), saved);
                    return saved;
                });
        willAnswer(invocation -> refreshTokenStore.remove(
                invocation.<RefreshToken>getArgument(0).getRefreshToken()))
                .given(refreshTokenRepository).delete(any(RefreshToken.class));

        tokenService = new TokenService(refreshTokenRepository);
        authApplication = new AuthApplication(tokenService, jwtProvider, jwtUtil, userDeviceService,
                userService, kakaoClient, appleService);
    }

    @DisplayName("재발급에 성공하면 기존 리프레시 토큰은 저장소에서 사라져 다시 재발급에 사용할 수 없다")
    @Test
    void reissue_afterRotation_rejectsReusedOldRefreshToken() {
        String oldRefreshToken = jwtProvider.generateRefreshToken(CustomAuthenticationToken.create(USER_ID));
        tokenService.saveRefreshToken(user, oldRefreshToken);

        ReissueResponse response = authApplication.reissue(oldRefreshToken);

        assertThat(response.refreshToken()).isNotEqualTo(oldRefreshToken);
        assertThat(refreshTokenStore).doesNotContainKey(oldRefreshToken);
        assertThatThrownBy(() -> authApplication.reissue(oldRefreshToken))
                .isInstanceOf(CustomAuthException.class)
                .extracting(throwable -> ((CustomAuthException) throwable).getICustomError())
                .isEqualTo(INVALID_TOKEN);
    }

    @DisplayName("재발급된 리프레시 토큰은 저장되어 이어서 다시 재발급할 수 있다")
    @Test
    void reissue_newRefreshToken_isStoredAndUsableForNextReissue() {
        String oldRefreshToken = jwtProvider.generateRefreshToken(CustomAuthenticationToken.create(USER_ID));
        tokenService.saveRefreshToken(user, oldRefreshToken);

        ReissueResponse first = authApplication.reissue(oldRefreshToken);

        assertThat(refreshTokenStore).containsOnlyKeys(first.refreshToken());
        assertThat(refreshTokenStore.get(first.refreshToken()).getUserId()).isEqualTo(USER_ID);

        ReissueResponse second = authApplication.reissue(first.refreshToken());

        assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());
        assertThat(refreshTokenStore).containsOnlyKeys(second.refreshToken());
    }
}
