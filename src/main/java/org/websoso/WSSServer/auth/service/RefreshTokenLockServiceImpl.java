package org.websoso.WSSServer.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenLockServiceImpl implements RefreshTokenLockService {

    // 잠금 키에는 raw Refresh Token 대신 해시를 사용해 민감한 토큰 값이 Redis에 남지 않도록 한다.
    private static final String LOCK_KEY_PREFIX = "refreshTokenLock:";
    private static final String HASH_ALGORITHM = "SHA-256";

    // 잠금 보유 중 프로세스가 죽어도 잠금이 남지 않도록 만료 시간을 둔다.
    private static final Duration LOCK_TIME_TO_LIVE = Duration.ofSeconds(5);

    // 소유자가 일치할 때만 삭제해, 만료 후 다른 요청이 획득한 잠금을 해제하지 않는다.
    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean tryLock(String refreshToken, String owner) {
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(toLockKey(refreshToken), owner, LOCK_TIME_TO_LIVE);
        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public boolean unlock(String refreshToken, String owner) {
        Long deletedCount = stringRedisTemplate.execute(UNLOCK_SCRIPT, List.of(toLockKey(refreshToken)), owner);
        return deletedCount != null && deletedCount > 0;
    }

    private String toLockKey(String refreshToken) {
        return LOCK_KEY_PREFIX + hash(refreshToken);
    }

    private String hash(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance(HASH_ALGORITHM)
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(HASH_ALGORITHM + " is not available", e);
        }
    }
}
