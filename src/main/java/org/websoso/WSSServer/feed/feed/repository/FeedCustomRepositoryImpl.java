package org.websoso.WSSServer.feed.feed.repository;

import static org.websoso.WSSServer.domain.QGenre.genre;
import static org.websoso.WSSServer.feed.feed.domain.QFeed.feed;
import static org.websoso.WSSServer.feed.feed.domain.QLike.like;
import static org.websoso.WSSServer.library.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;
import static org.websoso.WSSServer.novel.domain.QNovelGenre.novelGenre;
import static org.websoso.WSSServer.user.domain.QBlock.block;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.feed.feed.domain.QFeed;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.domain.common.SortCriteria;

@Repository
@RequiredArgsConstructor
public class FeedCustomRepositoryImpl implements FeedCustomRepository {

    private static final long NO_CURSOR = 0L;
    private static final long POPULAR_FEED_LIKE_COUNT = 5;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Feed> findPopularFeedsByNovelIds(List<Long> novelIds, List<Long> blockedUserIds) {
        return novelIds.stream()
                .map(novelId -> jpaQueryFactory
                        .selectFrom(feed)
                        .leftJoin(feed.likes, like)
                        .where(
                                feed.novelId.eq(novelId),
                                feed.isPublic.isTrue(),
                                feed.isSpoiler.isFalse(),
                                excludeBlockedUsers(blockedUserIds)
                        )
                        .groupBy(feed.feedId)
                        .orderBy(like.count().desc())
                        .fetchFirst())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Slice<Feed> findFeedsByNoOffsetPagination(User owner, Long lastFeedId, int size, Boolean isVisible,
                                                     Boolean isUnVisible, SortCriteria sortCriteria,
                                                     List<Genre> genres, Long visitorId, boolean isNotNovelConnect) {
        PageRequest pageRequest = PageRequest.of(0, size);

        List<Feed> feeds = jpaQueryFactory
                .selectFrom(feed)
                .distinct()
                .join(feed.user).fetchJoin()
                .leftJoin(novel).on(feed.novelId.eq(novel.novelId))
                .leftJoin(novelGenre).on(novel.eq(novelGenre.novel))
                .leftJoin(genre).on(novelGenre.genre.eq(genre))
                .where(
                        feed.user.eq(owner),
                        userFeedCursor(lastFeedId, sortCriteria),
                        checkVisible(visitorId),
                        checkPublic(isVisible, isUnVisible),
                        checkHidden(),
                        checkGenresAndNovels(genres, isNotNovelConnect)
                )
                .orderBy(
                        checkSortCriteria(sortCriteria),
                        checkFeedIdSortCriteria(sortCriteria))
                .limit(pageRequest.getPageSize() + 1L)
                .fetch();

        boolean hasNext = feeds.size() > pageRequest.getPageSize();

        if (hasNext) {
            feeds.remove(feeds.size() - 1);
        }

        return new SliceImpl<>(feeds, pageRequest, hasNext);
    }

    @Override
    public Slice<Feed> findFeeds(Long lastFeedId, Long userId, PageRequest pageRequest, List<Long> blockedUserIds) {
        List<Feed> feeds = jpaQueryFactory
                .selectFrom(feed)
                .join(feed.user).fetchJoin()
                .where(
                        ltFeedId(lastFeedId),
                        checkHidden(),
                        checkFeedListVisibility(userId),
                        excludeBlockedUsers(blockedUserIds)
                )
                .orderBy(feed.feedId.desc())
                .limit(pageRequest.getPageSize() + 1L)
                .fetch();

        boolean hasNext = feeds.size() > pageRequest.getPageSize();

        if (hasNext) {
            feeds.remove(feeds.size() - 1);
        }

        return new SliceImpl<>(feeds, pageRequest, hasNext);
    }

    @Override
    public Long countVisibleFeeds(User owner, Boolean isVisible,
                                  Boolean isUnVisible, List<Genre> genres,
                                  Long visitorId, boolean isNotNovelConnect) {
        return jpaQueryFactory
                .select(feed.feedId.countDistinct())
                .from(feed)
                .leftJoin(novel).on(feed.novelId.eq(novel.novelId))
                .leftJoin(novelGenre).on(novel.eq(novelGenre.novel))
                .leftJoin(genre).on(novelGenre.genre.eq(genre))
                .where(
                        feed.user.eq(owner),
                        checkVisible(visitorId),
                        checkPublic(isVisible, isUnVisible),
                        checkHidden(),
                        checkGenresAndNovels(genres, isNotNovelConnect)
                )
                .fetchOne();
    }

    @Override
    public Slice<Feed> findFeedsByNovelId(Long novelId, Long lastFeedId, Long userId, PageRequest pageRequest) {
        List<Feed> feeds = jpaQueryFactory
                .selectFrom(feed)
                .join(feed.user).fetchJoin()
                .where(
                        feed.novelId.eq(novelId),
                        ltFeedId(lastFeedId),
                        checkHidden(),
                        checkVisible(userId),
                        checkBlockRelation(userId)
                )
                .orderBy(feed.feedId.desc())
                .limit(pageRequest.getPageSize() + 1)
                .fetch();

        boolean hasNext = feeds.size() > pageRequest.getPageSize();

        if (hasNext) {
            feeds.remove(feeds.size() - 1);
        }

        return new SliceImpl<>(feeds, pageRequest, hasNext);
    }

    private BooleanExpression ltFeedId(Long lastFeedId) {
        if (lastFeedId == NO_CURSOR) {
            return null;
        }
        return feed.feedId.lt(lastFeedId);
    }

    private BooleanExpression userFeedCursor(Long lastFeedId, SortCriteria sortCriteria) {
        if (lastFeedId == NO_CURSOR) {
            return null;
        }

        QFeed cursorFeed = new QFeed("cursorFeed");

        BooleanExpression sameCreatedDate = feed.createdDate.eq(
                JPAExpressions
                        .select(cursorFeed.createdDate)
                        .from(cursorFeed)
                        .where(cursorFeed.feedId.eq(lastFeedId))
        );

        if (sortCriteria != null && sortCriteria.isOld()) {
            BooleanExpression nextByFeedId = sameCreatedDate.and(feed.feedId.gt(lastFeedId));

            return feed.createdDate.gt(
                    JPAExpressions
                            .select(cursorFeed.createdDate)
                            .from(cursorFeed)
                            .where(cursorFeed.feedId.eq(lastFeedId))
            ).or(nextByFeedId);
        }

        BooleanExpression nextByFeedId = sameCreatedDate.and(feed.feedId.lt(lastFeedId));

        return feed.createdDate.lt(
                JPAExpressions
                        .select(cursorFeed.createdDate)
                        .from(cursorFeed)
                        .where(cursorFeed.feedId.eq(lastFeedId))
        ).or(nextByFeedId);
    }

    private BooleanExpression checkPublic(Boolean isVisible, Boolean isUnVisible) {
        if (Boolean.TRUE.equals(isVisible) && Boolean.TRUE.equals(isUnVisible)) {
            return null;
        }
        if (Boolean.FALSE.equals(isVisible) && Boolean.FALSE.equals(isUnVisible)) {
            return Expressions.FALSE;
        }
        if (Boolean.TRUE.equals(isVisible)) {
            return feed.isPublic.eq(true);
        }
        if (Boolean.TRUE.equals(isUnVisible)) {
            return feed.isPublic.eq(false);
        }
        return null;
    }

    private OrderSpecifier<?> checkSortCriteria(SortCriteria sortCriteria) {
        if (sortCriteria != null && sortCriteria.equals(SortCriteria.OLD)) {
            return new OrderSpecifier<>(Order.ASC, feed.createdDate);
        }
        return new OrderSpecifier<>(Order.DESC, feed.createdDate);
    }

    private OrderSpecifier<?> checkFeedIdSortCriteria(SortCriteria sortCriteria) {
        if (sortCriteria != null && sortCriteria.equals(SortCriteria.OLD)) {
            return new OrderSpecifier<>(Order.ASC, feed.feedId);
        }
        return new OrderSpecifier<>(Order.DESC, feed.feedId);
    }

    @Override
    public Slice<Feed> findRecommendedFeeds(Long lastFeedId, Long userId, PageRequest pageRequest, List<Genre> genres,
                                            List<Long> blockedUserIds) {
        List<Feed> feeds = jpaQueryFactory
                .selectFrom(feed)
                .distinct()
                .join(feed.user).fetchJoin()
                .leftJoin(novel).on(feed.novelId.eq(novel.novelId))
                .leftJoin(novelGenre).on(novel.eq(novelGenre.novel))
                .leftJoin(genre).on(novelGenre.genre.eq(genre))
                .where(
                        ltFeedId(lastFeedId),
                        recommendedFeedCondition(userId, genres),
                        excludeBlockedUsers(blockedUserIds),
                        checkHidden(),
                        checkVisible(userId)
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

    @Override
    public List<Feed> findPopularRecommendedFeeds(Long userId, int size, List<Genre> genres,
                                                  List<Long> blockedUserIds) {
        return jpaQueryFactory
                .selectFrom(feed)
                .distinct()
                .join(feed.user).fetchJoin()
                .leftJoin(novel).on(feed.novelId.eq(novel.novelId))
                .leftJoin(novelGenre).on(novel.eq(novelGenre.novel))
                .leftJoin(genre).on(novelGenre.genre.eq(genre))
                .where(
                        recommendedFeedCondition(userId, genres),
                        excludeBlockedUsers(blockedUserIds),
                        checkHidden(),
                        feed.isPublic.isTrue()
                )
                .orderBy(feed.feedId.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression checkPopularFeed() {
        return JPAExpressions
                .select(like.count())
                .from(like)
                .where(like.feed.eq(feed))
                .goe(POPULAR_FEED_LIKE_COUNT);
    }

    private BooleanExpression recommendedFeedCondition(Long userId, List<Genre> genres) {
        BooleanExpression condition = checkPopularFeed();

        BooleanExpression preferredGenreCondition = checkPreferredGenres(genres);
        if (preferredGenreCondition != null) {
            condition = condition.or(preferredGenreCondition);
        }

        BooleanExpression interestedNovelCondition = checkInterestedNovel(userId);
        if (interestedNovelCondition != null) {
            condition = condition.or(interestedNovelCondition);
        }

        return condition;
    }

    private BooleanExpression checkPreferredGenres(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return null;
        }

        return genre.in(genres);
    }

    private BooleanExpression checkInterestedNovel(Long userId) {
        if (userId == null) {
            return null;
        }

        return JPAExpressions
                .selectOne()
                .from(userNovel)
                .where(
                        userNovel.user.userId.eq(userId),
                        userNovel.isInterest.isTrue(),
                        userNovel.novel.novelId.eq(feed.novelId)
                )
                .exists();
    }

    private BooleanExpression checkGenresAndNovels(List<Genre> genres, boolean isNotNovelConnect) {
        if (genres != null && !genres.isEmpty()) {
            BooleanExpression genreCondition = genre.in(genres);
            BooleanExpression novelConnectCondition = isNotNovelConnect ? feed.novelId.isNull() : null;
            return novelConnectCondition != null ? genreCondition.or(novelConnectCondition) : genreCondition;
        }
        return null;
    }

    private BooleanExpression excludeBlockedUsers(List<Long> blockedUserIds) {
        if (blockedUserIds == null || blockedUserIds.isEmpty()) {
            return null;
        }

        return feed.user.userId.notIn(blockedUserIds);
    }

    private BooleanExpression checkBlockRelation(Long userId) {
        if (userId == null) {
            return null;
        }

        return feed.user.userId.notIn(
                JPAExpressions
                        .select(block.blockedId)
                        .from(block)
                        .where(block.blockingId.eq(userId))
        ).and(feed.user.userId.notIn(
                JPAExpressions
                        .select(block.blockingId)
                        .from(block)
                        .where(block.blockedId.eq(userId))
        ));
    }

    private BooleanExpression checkHidden() {
        return feed.isHidden.eq(false);
    }

    private BooleanExpression checkVisible(Long userId) {
        if (userId == null) {
            return feed.isPublic.isTrue();
        }

        return feed.isPublic.isTrue().or(feed.user.userId.eq(userId));
    }

    private BooleanExpression checkFeedListVisibility(Long userId) {
        if (userId == null) {
            return feed.isPublic.isTrue();
        }

        return feed.isPublic.isTrue().or(feed.user.userId.eq(userId));
    }

}
