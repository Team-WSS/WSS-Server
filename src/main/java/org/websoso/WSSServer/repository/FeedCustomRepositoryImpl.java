package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QFeed.feed;
import static org.websoso.WSSServer.domain.QFeedImage.feedImage;
import static org.websoso.WSSServer.domain.QGenre.genre;
import static org.websoso.WSSServer.domain.QLike.like;
import static org.websoso.WSSServer.domain.QNovel.novel;
import static org.websoso.WSSServer.domain.QNovelGenre.novelGenre;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.FeedImage;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.FeedImageType;
import org.websoso.WSSServer.domain.common.SortCriteria;

@Repository
@RequiredArgsConstructor
public class FeedCustomRepositoryImpl implements FeedCustomRepository, FeedImageCustomRepository {

    private static final long NO_CURSOR = 0L;
    private static final int THUMBNAIL_IMAGE_COUNT = 1;
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
    public List<Feed> findFeedsByNoOffsetPagination(User owner, Long lastFeedId, int size, boolean isVisible,
                                                    boolean isUnVisible, SortCriteria sortCriteria,
                                                    List<Genre> genres) {
        return jpaQueryFactory
                .selectFrom(feed)
                .join(novel).on(feed.novelId.eq(novel.novelId))
                .join(novelGenre).on(novel.eq(novelGenre.novel))
                .join(genre).on(novelGenre.genre.eq(genre))
                .where(
                        feed.user.eq(owner),
                        ltFeedId(lastFeedId),
                        eqVisible(isVisible),
                        eqUnVisible(isUnVisible),
                        genre.in(genres)
                )
                .orderBy(
                        checkSortCriteria(sortCriteria),
                        feed.feedId.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public Optional<FeedImage> findThumbnailFeedImageByFeedId(long feedId) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(feedImage)
                .where(
                        feedImage.feedId.eq(feedId),
                        feedImage.feedImageType.eq(FeedImageType.FEED_THUMBNAIL)
                )
                .orderBy(feedImage.sequence.asc())
                .limit(THUMBNAIL_IMAGE_COUNT)
                .fetchOne());
    }

    private BooleanExpression ltFeedId(Long lastFeedId) {
        if (lastFeedId == NO_CURSOR) {
            return null;
        }
        return feed.feedId.lt(lastFeedId);
    }

    private BooleanExpression eqVisible(boolean isVisible) {
        return feed.isPublic.eq(isVisible);
    }

    private BooleanExpression eqUnVisible(boolean isUnVisible) {
        return feed.isPublic.eq(isUnVisible);
    }

    private OrderSpecifier<?> checkSortCriteria(SortCriteria sortCriteria) {
        if (sortCriteria.equals(SortCriteria.OLD)) {
            return new OrderSpecifier<>(Order.ASC, feed.createdDate);
        }
        return new OrderSpecifier<>(Order.DESC, feed.createdDate);
    }
}
