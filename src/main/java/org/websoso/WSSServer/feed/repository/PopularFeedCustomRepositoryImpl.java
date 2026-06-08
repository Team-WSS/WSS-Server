package org.websoso.WSSServer.feed.repository;

import static org.websoso.WSSServer.feed.domain.QFeed.feed;
import static org.websoso.WSSServer.feed.domain.QPopularFeed.popularFeed;
import static org.websoso.WSSServer.user.domain.QBlock.block;
import static org.websoso.WSSServer.user.domain.QUser.user;


import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.domain.PopularFeed;

@Repository
@RequiredArgsConstructor
public class PopularFeedCustomRepositoryImpl implements PopularFeedCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PopularFeed> findTodayPopularFeeds(Long userId, int size) {
        List<Long> blockIds = getBlockIds(userId);

        return jpaQueryFactory
                .selectFrom(popularFeed)
                .join(popularFeed.feed, feed)
                .join(feed.user, user)
                .where(
                        feed.isPublic.isTrue(),
                        feed.isHidden.isFalse(),
                        user.userId.notIn(blockIds)
                )
                .orderBy(popularFeed.popularFeedId.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<PopularFeed> findOrderByPopularFeedIdDesc(int size) {
        return jpaQueryFactory
                .selectFrom(popularFeed)
                .join(popularFeed.feed, feed)
                .where(
                        feed.isPublic.isTrue(),
                        feed.isHidden.isFalse()
                )
                .orderBy(popularFeed.popularFeedId.desc())
                .limit(size)
                .fetch();
    }

    /**
     * 본인이 차단했거나, 본인을 차단한 사용자의 ID를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 차단 사용자 ID
     */
    private List<Long> getBlockIds(Long userId) {
        NumberExpression<Long> blockUserId = new CaseBuilder()
                .when(block.blockingId.eq(userId)).then(block.blockedId)
                .otherwise(block.blockingId);

        return jpaQueryFactory
                .select(blockUserId)
                .from(block)
                .where(
                        block.blockingId.eq(userId)
                                .or(block.blockedId.eq(userId))
                )
                .distinct()
                .fetch();
    }
}
