package org.websoso.WSSServer.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

class RefreshTokenLockServiceImplTest {

    private static final String REFRESH_TOKEN = "header.payload.signature";
    private static final String OTHER_REFRESH_TOKEN = "header.other-payload.other-signature";
    private static final String OWNER = "owner-of-this-request";
    private static final String OTHER_OWNER = "owner-of-another-request";

    @ExtendWith(MockitoExtension.class)
    @Nested
    class TryLock {

        @Mock
        private StringRedisTemplate stringRedisTemplate;

        @Mock
        private ValueOperations<String, String> valueOperations;

        private RefreshTokenLockServiceImpl refreshTokenLockService;

        @BeforeEach
        void setUp() {
            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            refreshTokenLockService = new RefreshTokenLockServiceImpl(stringRedisTemplate);
        }

        @DisplayName("잠금은 아직 없을 때만 만료 시간과 함께 요청별 소유자 값으로 설정한다")
        @Test
        void tryLock_setsOwnerOnlyIfAbsentWithExpiration() {
            ArgumentCaptor<Duration> timeToLiveCaptor = ArgumentCaptor.forClass(Duration.class);
            given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);

            boolean acquired = refreshTokenLockService.tryLock(REFRESH_TOKEN, OWNER);

            assertThat(acquired).isTrue();
            then(valueOperations).should().setIfAbsent(anyString(), eq(OWNER), timeToLiveCaptor.capture());
            assertThat(timeToLiveCaptor.getValue()).isPositive();
        }

        @DisplayName("잠금 키에는 raw 리프레시 토큰 대신 해시를 사용한다")
        @Test
        void tryLock_doesNotExposeRawRefreshTokenInKey() {
            ArgumentCaptor<String> lockKeyCaptor = ArgumentCaptor.forClass(String.class);
            given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);

            refreshTokenLockService.tryLock(REFRESH_TOKEN, OWNER);

            then(valueOperations).should().setIfAbsent(lockKeyCaptor.capture(), anyString(), any(Duration.class));
            assertThat(lockKeyCaptor.getValue()).doesNotContain(REFRESH_TOKEN);
            assertThat(lockKeyCaptor.getValue()).startsWith("refreshTokenLock:");
        }

        @DisplayName("같은 토큰은 같은 잠금 키를, 다른 토큰은 다른 잠금 키를 사용한다")
        @Test
        void tryLock_usesTokenScopedKey() {
            ArgumentCaptor<String> lockKeyCaptor = ArgumentCaptor.forClass(String.class);
            given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);

            refreshTokenLockService.tryLock(REFRESH_TOKEN, OWNER);
            refreshTokenLockService.tryLock(REFRESH_TOKEN, OTHER_OWNER);
            refreshTokenLockService.tryLock(OTHER_REFRESH_TOKEN, OWNER);

            then(valueOperations).should(org.mockito.Mockito.times(3))
                    .setIfAbsent(lockKeyCaptor.capture(), anyString(), any(Duration.class));
            List<String> lockKeys = lockKeyCaptor.getAllValues();
            assertThat(lockKeys.get(0)).isEqualTo(lockKeys.get(1));
            assertThat(lockKeys.get(2)).isNotEqualTo(lockKeys.get(0));
        }

        @DisplayName("이미 다른 요청이 잠금을 보유하고 있으면 대기하지 않고 실패한다")
        @Test
        void tryLock_returnsFalseWhenAlreadyHeld() {
            given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(false);

            assertThat(refreshTokenLockService.tryLock(REFRESH_TOKEN, OWNER)).isFalse();
        }

        @DisplayName("설정 결과를 알 수 없으면 잠금을 획득하지 않은 것으로 본다")
        @Test
        void tryLock_returnsFalseWhenResultIsNull() {
            given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(null);

            assertThat(refreshTokenLockService.tryLock(REFRESH_TOKEN, OWNER)).isFalse();
        }
    }

    /**
     * 해제 스크립트가 실제 Redis에서 하는 일을 그대로 흉내 내어, 서비스가 넘기는 KEYS/ARGV 조합만으로
     * 소유자 비교 후 삭제가 성립하는지 확인한다. 스크립트 본문 자체는 별도로 검증한다.
     */
    @ExtendWith(MockitoExtension.class)
    @Nested
    class Unlock {

        private final Map<String, String> redis = new HashMap<>();

        @Mock
        private StringRedisTemplate stringRedisTemplate;

        private RefreshTokenLockServiceImpl refreshTokenLockService;

        @BeforeEach
        void setUp() {
            given(stringRedisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                    .willAnswer(invocation -> {
                        String lockKey = invocation.<List<String>>getArgument(1).get(0);
                        String owner = invocation.getArgument(2);
                        if (!owner.equals(redis.get(lockKey))) {
                            return 0L;
                        }
                        redis.remove(lockKey);
                        return 1L;
                    });
            refreshTokenLockService = new RefreshTokenLockServiceImpl(stringRedisTemplate);
        }

        @DisplayName("해제 스크립트는 소유자가 일치할 때만 삭제하도록 작성되어 있다")
        @Test
        void unlock_scriptComparesOwnerBeforeDeleting() {
            ArgumentCaptor<RedisScript<Long>> scriptCaptor = ArgumentCaptor.forClass(RedisScript.class);

            refreshTokenLockService.unlock(REFRESH_TOKEN, OWNER);

            then(stringRedisTemplate).should().execute(scriptCaptor.capture(), anyList(), anyString());
            String script = scriptCaptor.getValue().getScriptAsString().replace(" ", "");
            assertThat(script).contains("redis.call('get',KEYS[1])==ARGV[1]");
            assertThat(script).contains("redis.call('del',KEYS[1])");
            assertThat(scriptCaptor.getValue().getResultType()).isEqualTo(Long.class);
        }

        @DisplayName("자신이 보유한 잠금은 해제한다")
        @Test
        void unlock_releasesOwnLock() {
            holdLock(REFRESH_TOKEN, OWNER);

            assertThat(refreshTokenLockService.unlock(REFRESH_TOKEN, OWNER)).isTrue();
            assertThat(redis).isEmpty();
        }

        @DisplayName("만료 등으로 다른 요청이 획득한 잠금은 해제하지 못하고 그대로 남긴다")
        @Test
        void unlock_doesNotReleaseLockHeldByAnotherOwner() {
            holdLock(REFRESH_TOKEN, OTHER_OWNER);

            assertThat(refreshTokenLockService.unlock(REFRESH_TOKEN, OWNER)).isFalse();
            assertThat(redis).containsEntry(lockKeyOf(REFRESH_TOKEN), OTHER_OWNER);
        }

        @DisplayName("이미 만료되어 사라진 잠금을 해제하면 해제하지 않은 것으로 본다")
        @Test
        void unlock_returnsFalseWhenLockAlreadyExpired() {
            assertThat(refreshTokenLockService.unlock(REFRESH_TOKEN, OWNER)).isFalse();
        }

        private void holdLock(String refreshToken, String owner) {
            redis.put(lockKeyOf(refreshToken), owner);
        }
    }

    // 서비스와 동일한 규칙(prefix + SHA-256 hex)으로 잠금 키를 계산한다.
    private static String lockKeyOf(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return "refreshTokenLock:" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
