package org.websoso.WSSServer.auth.service;

/**
 * 동일한 Refresh Token으로 들어온 재발급 요청을 토큰 단위로 직렬화하기 위한 잠금이다.
 * 서로 다른 Refresh Token은 서로 다른 잠금을 사용하므로 병렬로 처리된다.
 */
public interface RefreshTokenLockService {

    /**
     * 해당 Refresh Token의 잠금을 획득한다. 이미 다른 요청이 잠금을 보유하고 있으면 대기하지 않고 즉시 실패한다.
     *
     * @param owner 요청별로 생성한 소유자 값
     * @return 잠금을 획득했으면 true
     */
    boolean tryLock(String refreshToken, String owner);

    /**
     * 잠금을 해제한다. 소유자가 일치할 때만 해제되며, 비교와 삭제는 원자적으로 수행된다.
     *
     * @return 자신이 보유한 잠금을 해제했으면 true
     */
    boolean unlock(String refreshToken, String owner);
}
