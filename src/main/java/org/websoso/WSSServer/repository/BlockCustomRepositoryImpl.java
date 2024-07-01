package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QBlock.block;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class BlockCustomRepositoryImpl implements BlockCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Boolean existsByTwoUserId(Long blockedId, Long blockingId) {
        return jpaQueryFactory
                .selectOne()
                .from(block)
                .where(block.blockedId.eq(blockedId).and(block.blockingId.eq(blockingId))
                        .or(block.blockedId.eq(blockingId).and(block.blockingId.eq(blockedId))))
                .fetchFirst() != null;
    }
}
