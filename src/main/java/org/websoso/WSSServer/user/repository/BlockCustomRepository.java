package org.websoso.WSSServer.user.repository;

import java.util.List;

public interface BlockCustomRepository {

    boolean existsBlockRelation(Long userId, Long targetUserId);

    List<Long> findBlockRelationUserIds(Long userId);
}
