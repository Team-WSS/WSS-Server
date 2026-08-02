package org.websoso.WSSServer.user.repository;

import static org.websoso.WSSServer.user.domain.QBlock.block;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlockCustomRepositoryImpl implements BlockCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsBlockRelation(Long userId, Long targetUserId) {
        return jpaQueryFactory
                .selectOne()
                .from(block)
                .where(
                        block.blockingId.eq(userId).and(block.blockedId.eq(targetUserId))
                                .or(block.blockingId.eq(targetUserId).and(block.blockedId.eq(userId)))
                )
                .fetchFirst() != null;
    }

    @Override
    public List<Long> findBlockRelationUserIds(Long userId) {
        Expression<Long> relatedUserId = new CaseBuilder()
                .when(block.blockingId.eq(userId))
                .then(block.blockedId)
                .otherwise(block.blockingId);

        return jpaQueryFactory
                .select(relatedUserId)
                .distinct()
                .from(block)
                .where(block.blockingId.eq(userId).or(block.blockedId.eq(userId)))
                .fetch();
    }
}
