package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QBlock.block;
import static org.websoso.WSSServer.domain.QFeed.feed;
import static org.websoso.WSSServer.domain.QFeedImage.feedImage;
import static org.websoso.WSSServer.domain.QGenre.genre;
import static org.websoso.WSSServer.domain.QLike.like;
import static org.websoso.WSSServer.domain.QNovel.novel;
import static org.websoso.WSSServer.domain.QNovelGenre.novelGenre;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
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
    private static final long POPULAR_FEED_LIKE_COUNT = 5;
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
    public List<Feed> findFeedsByNoOffsetPagination(User owner, Long lastFeedId, int size, Boolean isVisible,
                                                    Boolean isUnVisible, SortCriteria sortCriteria,
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
                        checkGenres(genres)
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

    private BooleanExpression eqVisible(Boolean isVisible) {
        if (isVisible != null) {
            return feed.isPublic.eq(isVisible);
        }
        return null;
    }

    private BooleanExpression eqUnVisible(Boolean isUnVisible) {
        if (isUnVisible != null) {
            return feed.isPublic.eq(isUnVisible);
        }
        return null;
    }

    private OrderSpecifier<?> checkSortCriteria(SortCriteria sortCriteria) {
        if (sortCriteria != null && sortCriteria.equals(SortCriteria.OLD)) {
            return new OrderSpecifier<>(Order.ASC, feed.createdDate);
        }
        return new OrderSpecifier<>(Order.DESC, feed.createdDate);
    }

    @Override
    public Slice<Feed> findRecommendedFeeds(Long lastFeedId, Long userId, PageRequest pageRequest, List<Genre> genres) {
        List<Feed> feeds = jpaQueryFactory
                .selectFrom(feed)
                .join(novel).on(feed.novelId.eq(novel.novelId))
                .join(novelGenre).on(novel.eq(novelGenre.novel))
                .join(genre).on(novelGenre.genre.eq(genre))
                .where(
                        ltFeedId(lastFeedId),
                        checkPopularFeed(),
                        checkGenres(genres),
                        checkBlocking(userId),
                        checkHidden()
                )
                .limit(pageRequest.getPageSize() + 1)
                .orderBy(feed.feedId.desc())
                .fetch();

        boolean hasNext = feeds.size() > pageRequest.getPageSize();

        if (hasNext) {
            feeds.remove(feeds.size() - 1);
        }

        return new SliceImpl<>(feeds, pageRequest, hasNext);
    }

    private BooleanExpression checkPopularFeed() {
        return JPAExpressions
                .select(like.count())
                .from(like)
                .where(like.feed.eq(feed))
                .goe(POPULAR_FEED_LIKE_COUNT);
    }

    private BooleanExpression checkGenres(List<Genre> genres) {
        if (genres != null && !genres.isEmpty()) {
            return genre.in(genres);
        }
        return null;
    }

    private BooleanExpression checkBlocking(Long userId) {
        if (userId != null) {
            return feed.user.userId.notIn(
                    JPAExpressions
                            .select(block.blockedId)
                            .from(block)
                            .where(block.blockingId.eq(userId)) // userId는 파라미터
            );
        }
        return null;
    }

    private BooleanExpression checkHidden() {
        return feed.isHidden.eq(false);
    }
}
