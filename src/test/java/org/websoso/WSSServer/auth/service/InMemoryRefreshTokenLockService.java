package org.websoso.WSSServer.auth.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Redis 없이 {@link RefreshTokenLockService} 계약을 그대로 흉내 내는 테스트 대역이다.
 * 토큰 단위 잠금, 요청별 소유자, 만료 시간, 소유자 비교 후 삭제라는 동작을 실제 구현과 동일하게 제공한다.
 */
public class InMemoryRefreshTokenLockService implements RefreshTokenLockService {

    private final Map<String, Holder> locks = new ConcurrentHashMap<>();
    private final Duration timeToLive;

    public InMemoryRefreshTokenLockService() {
        this(Duration.ofSeconds(5));
    }

    public InMemoryRefreshTokenLockService(Duration timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
    public boolean tryLock(String refreshToken, String owner) {
        long now = System.nanoTime();
        Holder holder = locks.compute(refreshToken, (key, current) -> {
            if (current != null && current.expiresAtNanos() - now > 0) {
                return current;
            }
            return new Holder(owner, now + timeToLive.toNanos());
        });
        return holder.owner().equals(owner);
    }

    @Override
    public boolean unlock(String refreshToken, String owner) {
        AtomicBoolean unlocked = new AtomicBoolean(false);
        locks.computeIfPresent(refreshToken, (key, current) -> {
            if (!current.owner().equals(owner)) {
                return current;
            }
            unlocked.set(true);
            return null;
        });
        return unlocked.get();
    }

    public boolean isLocked(String refreshToken) {
        Holder holder = locks.get(refreshToken);
        return holder != null && holder.expiresAtNanos() - System.nanoTime() > 0;
    }

    private record Holder(String owner, long expiresAtNanos) {
    }
}
