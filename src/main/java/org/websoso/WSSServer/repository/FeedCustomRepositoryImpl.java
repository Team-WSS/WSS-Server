package org.websoso.WSSServer.repository;


import static org.websoso.WSSServer.domain.QFeed.feed;
import static org.websoso.WSSServer.domain.QLike.like;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
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
    public List<Feed> findPopularFeedsByNovelIds(List<Long> novelIds) {
        return novelIds.stream()
                .map(novelId -> jpaQueryFactory
                        .selectFrom(feed)
                        .leftJoin(feed.likes, like)
                        .where(feed.novelId.eq(novelId))
                        .groupBy(feed.feedId)
                        .orderBy(like.count().desc())
                        .fetchFirst())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
