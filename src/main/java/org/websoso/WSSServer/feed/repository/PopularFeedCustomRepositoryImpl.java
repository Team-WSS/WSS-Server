package org.websoso.WSSServer.feed.repository;

import static org.websoso.WSSServer.feed.domain.QFeed.feed;
import static org.websoso.WSSServer.feed.domain.QPopularFeed.popularFeed;
import static org.websoso.WSSServer.user.domain.QBlock.block;
import static org.websoso.WSSServer.user.domain.QUser.user;


import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.domain.PopularFeed;

@Repository
@RequiredArgsConstructor
public class PopularFeedCustomRepositoryImpl implements PopularFeedCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PopularFeed> findTodayPopularFeeds(Long userId) {
        List<Long> blockingIds = jpaQueryFactory
                .select(block.blockedId)
                .from(block)
                .where(block.blockingId.eq(userId))
                .fetch();

        List<Long> blockedIds = jpaQueryFactory
                .select(block.blockingId)
                .from(block)
                .where(block.blockedId.eq(userId))
                .fetch();

        List<Long> blockIds = Stream.concat(blockingIds.stream(), blockedIds.stream())
                .distinct()
                .toList();

        return jpaQueryFactory
                .selectFrom(popularFeed)
                .join(popularFeed.feed, feed)
                .join(feed.user, user)
                .where(user.userId.notIn(blockIds),
                        popularFeed.feed.isPublic.isTrue(),
                        popularFeed.feed.isHidden.isFalse())
                .orderBy(popularFeed.popularFeedId.desc())
                .limit(9)
                .fetch();
    }

    @Override
    public List<PopularFeed> findTop9ByOrderByPopularFeedIdDesc() {
        return jpaQueryFactory
                .selectFrom(popularFeed)
                .join(popularFeed.feed, feed)
                .where(feed.isPublic.isTrue(),
                        feed.isHidden.isFalse())
                .orderBy(popularFeed.popularFeedId.desc())
                .limit(9)
                .fetch();
    }
}
