package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QFeed.feed;
import static org.websoso.WSSServer.domain.QFeedImage.feedImage;
import static org.websoso.WSSServer.domain.QLike.like;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.FeedImage;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.FeedImageType;

@Repository
@RequiredArgsConstructor
public class FeedCustomRepositoryImpl implements FeedCustomRepository, FeedImageCustomRepository {

    private static final long NO_CURSOR = 0L;
    private static final int THUMBNAIL_IMAGE_COUNT = 0;
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
    public FeedImage findThumbnailFeedImagwByFeedId(long feedId) {
        return jpaQueryFactory
                .select(feedImage)
                .from(feedImage)
                .where(feedImage.feedId.eq(feedId)
                        .and(feedImage.feedImageType.eq(FeedImageType.FEED_THUMBNAIL)))
                .orderBy(feedImage.sequence.asc())
                .limit(THUMBNAIL_IMAGE_COUNT)
                .fetchOne();
    }

    private BooleanExpression ltFeedId(Long lastFeedId) {
        if (lastFeedId == NO_CURSOR) {
            return null;
        }
        return feed.feedId.lt(lastFeedId);
    }
}
