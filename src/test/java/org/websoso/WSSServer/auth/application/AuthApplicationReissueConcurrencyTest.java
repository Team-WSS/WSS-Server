package org.websoso.WSSServer.auth.application;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
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
import org.websoso.WSSServer.auth.service.InMemoryRefreshTokenLockService;
import org.websoso.WSSServer.auth.service.TokenService;
import org.websoso.WSSServer.exception.exception.CustomAuthException;
import org.websoso.WSSServer.notification.service.UserDeviceService;
import org.websoso.WSSServer.user.service.UserService;

/**
 * 저장소를 토큰 문자열 키 기반 맵으로 흉내 내고, 저장된 토큰을 조회하는 시점에 지연이나 동기화를 끼워 넣어
 * 동일 Refresh Token에 대한 동시 재발급 경합을 재현한다. 잠금이 없으면 여러 요청이 같은 토큰을 함께 조회해
 * 모두 회전에 성공하므로, 잠금 도입 전에는 "정확히 하나만 성공" 검증이 실패한다.
 */
@ExtendWith(MockitoExtension.class)
class AuthApplicationReissueConcurrencyTest {

    private static final Long USER_ID = 42L;
    private static final Long OTHER_USER_ID = 77L;
    private static final int CONCURRENT_REQUESTS = 8;
    private static final long LOOKUP_DELAY_MILLIS = 50L;
    private static final long AWAIT_TIMEOUT_SECONDS = 5L;

    private final JwtKeyProvider jwtKeyProvider = new JwtKeyProvider(TestTokenFactory.TEST_SECRET);
    private final JwtProvider jwtProvider = new JwtProvider(jwtKeyProvider,
            TestTokenFactory.ACCESS_TOKEN_EXPIRATION, TestTokenFactory.REFRESH_TOKEN_EXPIRATION);
    private final JWTUtil jwtUtil = new JWTUtil(jwtKeyProvider);
    private final Map<String, RefreshToken> refreshTokenStore = new ConcurrentHashMap<>();
    private final InMemoryRefreshTokenLockService refreshTokenLockService = new InMemoryRefreshTokenLockService();

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

    private AuthApplication authApplication;
    private ExecutorService executor;

    // 저장된 토큰을 조회한 직후 실행되어 경합 구간을 넓히거나 두 요청의 동시 진입을 확인한다.
    private volatile ThrowingRunnable afterLookup = () -> {
    };

    @BeforeEach
    void setUp() {
        given(refreshTokenRepository.findByRefreshToken(anyString()))
                .willAnswer(invocation -> {
                    Optional<RefreshToken> found = Optional.ofNullable(
                            refreshTokenStore.get(invocation.<String>getArgument(0)));
                    afterLookup.run();
                    return found;
                });
        given(refreshTokenRepository.save(any(RefreshToken.class)))
                .willAnswer(invocation -> {
                    RefreshToken saved = invocation.getArgument(0);
                    refreshTokenStore.put(saved.getRefreshToken(), saved);
                    return saved;
                });
        willAnswer(invocation -> refreshTokenStore.remove(
                invocation.<RefreshToken>getArgument(0).getRefreshToken()))
                .given(refreshTokenRepository).delete(any(RefreshToken.class));

        TokenService tokenService = new TokenService(refreshTokenRepository);
        authApplication = new AuthApplication(tokenService, refreshTokenLockService, jwtProvider, jwtUtil,
                userDeviceService, userService, kakaoClient, appleService);
        executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @DisplayName("동일한 리프레시 토큰으로 동시에 재발급을 요청하면 정확히 하나만 성공한다")
    @Test
    void reissue_concurrentRequestsWithSameToken_succeedExactlyOnce() throws Exception {
        String refreshToken = storeRefreshToken(USER_ID);
        afterLookup = () -> Thread.sleep(LOOKUP_DELAY_MILLIS);

        List<Future<ReissueResponse>> futures = submitConcurrentReissues(refreshToken, CONCURRENT_REQUESTS);

        List<ReissueResponse> succeeded = new ArrayList<>();
        int rejectedCount = 0;
        for (Future<ReissueResponse> future : futures) {
            try {
                succeeded.add(future.get(AWAIT_TIMEOUT_SECONDS, SECONDS));
            } catch (ExecutionException e) {
                assertThat(e.getCause()).isInstanceOf(CustomAuthException.class);
                rejectedCount++;
            }
        }

        assertThat(succeeded).hasSize(1);
        assertThat(rejectedCount).isEqualTo(CONCURRENT_REQUESTS - 1);
    }

    @DisplayName("동시 재발급 이후 저장소에는 성공한 요청이 발급한 리프레시 토큰만 남는다")
    @Test
    void reissue_concurrentRequestsWithSameToken_leaveOnlyTheWinningToken() throws Exception {
        String refreshToken = storeRefreshToken(USER_ID);
        afterLookup = () -> Thread.sleep(LOOKUP_DELAY_MILLIS);

        List<Future<ReissueResponse>> futures = submitConcurrentReissues(refreshToken, CONCURRENT_REQUESTS);
        List<ReissueResponse> succeeded = collectSucceeded(futures);

        assertThat(succeeded).hasSize(1);
        assertThat(refreshTokenStore).containsOnlyKeys(succeeded.get(0).refreshToken());
    }

    @DisplayName("동시 재발급이 끝나면 해당 토큰의 잠금은 남아 있지 않다")
    @Test
    void reissue_concurrentRequestsWithSameToken_releaseLockAfterCompletion() throws Exception {
        String refreshToken = storeRefreshToken(USER_ID);
        afterLookup = () -> Thread.sleep(LOOKUP_DELAY_MILLIS);

        collectSucceeded(submitConcurrentReissues(refreshToken, CONCURRENT_REQUESTS));

        assertThat(refreshTokenLockService.isLocked(refreshToken)).isFalse();
    }

    @DisplayName("서로 다른 리프레시 토큰의 재발급 요청은 서로를 막지 않고 동시에 진행된다")
    @Test
    void reissue_concurrentRequestsWithDifferentTokens_runInParallel() throws Exception {
        String refreshToken = storeRefreshToken(USER_ID);
        String otherRefreshToken = storeRefreshToken(OTHER_USER_ID);
        // 두 요청이 각자의 잠금을 잡은 채 동시에 조회 구간에 들어와야만 통과한다.
        CyclicBarrier bothInsideLock = new CyclicBarrier(2);
        afterLookup = () -> bothInsideLock.await(AWAIT_TIMEOUT_SECONDS, SECONDS);

        Future<ReissueResponse> first = executor.submit(() -> authApplication.reissue(refreshToken));
        Future<ReissueResponse> second = executor.submit(() -> authApplication.reissue(otherRefreshToken));

        ReissueResponse firstResponse = first.get(AWAIT_TIMEOUT_SECONDS, SECONDS);
        ReissueResponse secondResponse = second.get(AWAIT_TIMEOUT_SECONDS, SECONDS);
        assertThat(jwtUtil.getUserIdFromJwt(firstResponse.refreshToken())).isEqualTo(USER_ID);
        assertThat(jwtUtil.getUserIdFromJwt(secondResponse.refreshToken())).isEqualTo(OTHER_USER_ID);
        assertThat(refreshTokenStore).containsOnlyKeys(firstResponse.refreshToken(), secondResponse.refreshToken());
    }

    private String storeRefreshToken(Long userId) {
        String refreshToken = jwtProvider.generateRefreshToken(CustomAuthenticationToken.create(userId));
        refreshTokenStore.put(refreshToken, new RefreshToken(refreshToken, userId));
        return refreshToken;
    }

    private List<Future<ReissueResponse>> submitConcurrentReissues(String refreshToken, int count) {
        CyclicBarrier startLine = new CyclicBarrier(count);
        List<Future<ReissueResponse>> futures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            futures.add(executor.submit(() -> {
                startLine.await(AWAIT_TIMEOUT_SECONDS, SECONDS);
                return authApplication.reissue(refreshToken);
            }));
        }
        return futures;
    }

    private List<ReissueResponse> collectSucceeded(List<Future<ReissueResponse>> futures) throws Exception {
        List<ReissueResponse> succeeded = new ArrayList<>();
        for (Future<ReissueResponse> future : futures) {
            try {
                succeeded.add(future.get(AWAIT_TIMEOUT_SECONDS, SECONDS));
            } catch (ExecutionException e) {
                assertThat(e.getCause()).isInstanceOf(CustomAuthException.class);
            }
        }
        return succeeded;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {

        void run() throws Exception;
    }
}
