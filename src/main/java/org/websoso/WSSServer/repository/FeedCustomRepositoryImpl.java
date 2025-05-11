package org.websoso.WSSServer.repository;


import static org.websoso.WSSServer.domain.QFeed.feed;
import static org.websoso.WSSServer.domain.QFeedImage.feedImage;
import static org.websoso.WSSServer.domain.QLike.like;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.QFeedImage;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.FeedImageType;

@Repository
@RequiredArgsConstructor
public class FeedCustomRepositoryImpl implements FeedCustomRepository, FeedImageCustomRepository {

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

    @Override
    public FeedImageSummary findFeedThumbnailAndImageCountByFeedId(long feedId) {
        String thumbnailUrl = jpaQueryFactory
                .select(feedImage.url)
                .from(feedImage)
                .where(feedImage.feedId.eq(feedId)
                        .and(feedImage.feedImageType.eq(FeedImageType.FEED_THUMBNAIL)))
                .orderBy(feedImage.sequence.asc())
                .limit(1)
                .fetchOne();

        Long imageCount = jpaQueryFactory
                .select(feedImage.count())
                .from(feedImage)
                .where(feedImage.feedId.eq(feedId))
                .fetchOne();

        return new FeedImageSummary(thumbnailUrl, imageCount != null ? imageCount.intValue() : 0);
    }

    private BooleanExpression ltFeedId(Long lastFeedId) {
        if (lastFeedId == 0) {
            return null;
        }
        return feed.feedId.lt(lastFeedId);
    }
}
