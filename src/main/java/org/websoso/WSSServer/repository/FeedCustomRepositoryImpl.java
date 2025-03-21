package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QBlock.block;
import static org.websoso.WSSServer.domain.QFeed.feed;
import static org.websoso.WSSServer.domain.QLike.like;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;

@Repository
@RequiredArgsConstructor
public class FeedCustomRepositoryImpl implements FeedCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Map<Long, Feed> findPopularFeedsByNovelIds(User user, List<Long> novelIds) {
        if (novelIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> blockedUserIds = (user != null) ? jpaQueryFactory.select(block.blockedId).from(block)
                .where(block.blockingId.eq(user.getUserId())).fetch() : Collections.emptyList();

        List<Tuple> results = jpaQueryFactory
                .select(feed, like.count())
                .from(feed)
                .leftJoin(feed.likes, like)
                .where(feed.novelId.in(novelIds).and(feed.user.userId.notIn(blockedUserIds)))
                .groupBy(feed.feedId)
                .orderBy(like.count().desc())
                .fetch();

        return results
                .stream()
                .map(tuple -> tuple.get(feed))
                .collect(Collectors.toMap(
                        Feed::getNovelId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public List<Feed> findFeedsByNoOffsetPagination(User owner, Long lastFeedId, int size) {
        return jpaQueryFactory
                .selectFrom(feed)
                .where(
                        feed.user.eq(owner),
                        ltFeedId(lastFeedId)
                )
                .orderBy(feed.feedId.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression ltFeedId(Long lastFeedId) {
        if (lastFeedId == 0) {
            return null;
        }
        return feed.feedId.lt(lastFeedId);
    }
}
