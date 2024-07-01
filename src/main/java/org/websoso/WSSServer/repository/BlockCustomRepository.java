package org.websoso.WSSServer.repository;

public interface BlockCustomRepository {
    Boolean existsByTwoUserId(Long blockedId, Long blockingId);
}
