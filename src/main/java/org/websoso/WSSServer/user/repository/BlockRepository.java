package org.websoso.WSSServer.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.user.domain.Block;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockingIdAndBlockedId(Long blockingId, Long blockedId);

    List<Block> findByBlockingId(Long blockingId);

    @Query("""
                SELECT count(b) > 0
                FROM Block b
                WHERE (b.blockingId = :userId1 AND b.blockedId = :userId2)
                   OR (b.blockingId = :userId2 AND b.blockedId = :userId1)
            """)
    boolean existsBlockRelation(Long userId1, Long userId2);
}
