package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.user.domain.Block;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockingIdAndBlockedId(Long blockingId, Long blockedId);

    List<Block> findByBlockingId(Long blockingId);

}
