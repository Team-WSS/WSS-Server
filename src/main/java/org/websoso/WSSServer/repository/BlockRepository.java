package org.websoso.WSSServer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Block;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockingIdAndBlockedId(Long blockingId, Long blockedId);

    List<Block> findByBlockingId(Long blockingId);

    @Query("SELECT b FROM Block b WHERE (b.blockingId = :firstUserId AND b.blockedId = :secondUserId) OR (b.blockingId = :secondUserId AND b.blockedId = :firstUserId)")
    Optional<Block> findByTwoUserId(@Param("firstUserId") Long firstUserId, @Param("secondUserId") Long secondUserId);

}
