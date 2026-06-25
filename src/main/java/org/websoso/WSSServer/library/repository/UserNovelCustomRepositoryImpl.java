package org.websoso.WSSServer.library.repository;

import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;
import static org.websoso.WSSServer.library.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;
import static org.websoso.WSSServer.novel.domain.QNovelGenre.novelGenre;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.domain.common.UserNovelSortType;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;
import org.websoso.WSSServer.library.repository.cursor.UserNovelCursor;
import org.websoso.WSSServer.novel.domain.Novel;

@Repository
@RequiredArgsConstructor
public class UserNovelCustomRepositoryImpl implements UserNovelCustomRepository {

    private static final long NO_CURSOR = 0L;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public UserNovelCountGetResponse findUserNovelStatistics(Long userId) {
        return jpaQueryFactory
                .select(Projections.constructor(UserNovelCountGetResponse.class,
                        userNovel.isInterest
                                .when(true)
                                .then(1)
                                .otherwise(0)
                                .sum()
                                .coalesce(0),
                        userNovel.status
                                .when(WATCHING)
                                .then(1)
                                .otherwise(0)
                                .sum()
                                .coalesce(0),
                        userNovel.status
                                .when(WATCHED)
                                .then(1)
                                .otherwise(0)
                                .sum()
                                .coalesce(0),
                        userNovel.status
                                .when(QUIT)
                                .then(1)
                                .otherwise(0)
                                .sum()
                                .coalesce(0)
                ))
                .from(userNovel)
                .where(userNovel.user.userId.eq(userId))
                .fetchOne();
    }

    public List<Long> findTodayPopularNovelsId(Pageable pageable) {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        return jpaQueryFactory
                .select(userNovel.novel.novelId)
                .from(userNovel)
                .where((userNovel.status.eq(ReadStatus.WATCHING)
                        .or(userNovel.status.eq(ReadStatus.WATCHED))
                        .or(userNovel.isInterest.isTrue()))
                        .and(userNovel.createdDate.after(sevenDaysAgo.atStartOfDay())))
                .groupBy(userNovel.novel.novelId)
                .orderBy(userNovel.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Novel> findTasteNovels(List<Genre> preferGenres) {
        return jpaQueryFactory
                .select(userNovel.novel, userNovel.userNovelId)
                .from(userNovel)
                .join(userNovel.novel, novel)
                .join(novelGenre).on(novelGenre.novel.eq(novel))
                .where(novelGenre.genre.in(preferGenres))
                .orderBy(userNovel.userNovelId.desc())
                .fetch()
                .stream()
                .map(tuple -> tuple.get(userNovel.novel))
                .distinct()
                .limit(10)
                .toList();
    }

    @Override
    public List<UserNovel> findFilteredUserNovels(Long userId, Boolean isInterest, List<String> readStatuses,
                                                  List<String> attractivePoints, Float novelRating, String query,
                                                  Long lastUserNovelId, int size, boolean isAscending,
                                                  LocalDateTime updatedSince) {
        JPAQuery<UserNovel> queryBuilder = jpaQueryFactory
                .selectFrom(userNovel)
                .join(userNovel.novel, novel).fetchJoin()
                .where(userNovel.user.userId.eq(userId));

        applyFilters(queryBuilder, isInterest, readStatuses, attractivePoints, novelRating, query, updatedSince);

        queryBuilder.where(checkLastUserNovelId(lastUserNovelId, isAscending));
        queryBuilder.orderBy(checkSortOrder(isAscending));

        return queryBuilder.limit(size).fetch();
    }

    private BooleanExpression checkLastUserNovelId(Long lastUserNovelId, boolean isAscending) {
        if (lastUserNovelId == NO_CURSOR) {
            return null;
        }
        if (isAscending) {
            return userNovel.userNovelId.gt(lastUserNovelId);
        } else {
            return userNovel.userNovelId.lt(lastUserNovelId);
        }
    }

    private OrderSpecifier<?> checkSortOrder(boolean isAscending) {
        if (isAscending) {
            return new OrderSpecifier<>(Order.ASC, userNovel.userNovelId);
        }
        return new OrderSpecifier<>(Order.DESC, userNovel.userNovelId);
    }

    @Override
    public Long countByUserIdAndFilters(Long userId, Boolean isInterest, List<String> readStatuses,
                                        List<String> attractivePoints, Float novelRating, String query,
                                        LocalDateTime updatedSince) {
        JPAQuery<Long> queryBuilder = jpaQueryFactory
                .select(userNovel.count())
                .from(userNovel)
                .join(userNovel.novel, novel)
                .where(userNovel.user.userId.eq(userId));

        applyFilters(queryBuilder, isInterest, readStatuses, attractivePoints, novelRating, query, updatedSince);

        return queryBuilder.fetchOne();
    }

    @Override
    public List<UserNovel> findFilteredUserNovelsV2(Long userId, Boolean isInterest, List<String> readStatuses,
                                                    List<String> genres, Boolean isCompleted, Float ratingMin,
                                                    Float ratingMax, Boolean unratedOnly,
                                                    List<String> attractivePoints, List<String> keywords,
                                                    UserNovelCursor cursor, int size, UserNovelSortType sortType) {
        JPAQuery<UserNovel> queryBuilder = jpaQueryFactory
                .selectFrom(userNovel)
                .distinct()
                .join(userNovel.novel, novel).fetchJoin()
                .where(userNovel.user.userId.eq(userId));

        applyFiltersV2(queryBuilder, isInterest, readStatuses, genres, isCompleted, ratingMin, ratingMax,
                unratedOnly, attractivePoints, keywords);

        queryBuilder.where(cursorCondition(cursor, sortType));
        queryBuilder.orderBy(sortType.orderSpecifiers().toArray(OrderSpecifier[]::new));

        return queryBuilder.limit(size).fetch();
    }

    @Override
    public Long countByUserIdAndFiltersV2(Long userId, Boolean isInterest, List<String> readStatuses,
                                          List<String> genres, Boolean isCompleted, Float ratingMin, Float ratingMax,
                                          Boolean unratedOnly, List<String> attractivePoints, List<String> keywords) {
        JPAQuery<Long> queryBuilder = jpaQueryFactory
                .select(userNovel.countDistinct())
                .from(userNovel)
                .join(userNovel.novel, novel)
                .where(userNovel.user.userId.eq(userId));

        applyFiltersV2(queryBuilder, isInterest, readStatuses, genres, isCompleted, ratingMin, ratingMax,
                unratedOnly, attractivePoints, keywords);

        return queryBuilder.fetchOne();
    }

    private <T> void applyFilters(JPAQuery<T> queryBuilder, Boolean isInterest, List<String> readStatuses,
                                  List<String> attractivePoints, Float novelRating, String query,
                                  LocalDateTime updatedSince) {
        Optional.ofNullable(isInterest)
                .ifPresent(interest -> queryBuilder.where(userNovel.isInterest.eq(interest)));

        Optional.ofNullable(readStatuses)
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream().map(String::toUpperCase).map(ReadStatus::valueOf)
                        .collect(Collectors.toList()))
                .ifPresent(statusEnums -> queryBuilder.where(userNovel.status.in(statusEnums)));

        Optional.ofNullable(attractivePoints)
                .filter(list -> !list.isEmpty())
                .ifPresent(points -> queryBuilder.where(
                        userNovel.userNovelAttractivePoints.any().attractivePoint.attractivePointName.in(points)));

        Optional.ofNullable(novelRating)
                .ifPresent(rating -> queryBuilder.where(userNovel.userNovelRating.goe(rating)));

        Optional.ofNullable(query)
                .filter(q -> !q.isBlank())
                .ifPresent(q -> queryBuilder.where(
                        novel.title.containsIgnoreCase(q).or(novel.author.containsIgnoreCase(q))));

        Optional.ofNullable(updatedSince)
                .ifPresent(ts -> queryBuilder.where(userNovel.modifiedDate.gt(ts)));
    }

    // 전달받은 QueryDSL 쿼리 객체에 필터 조건을 누적해서 적용한다.
    private <T> void applyFiltersV2(JPAQuery<T> queryBuilder, Boolean isInterest, List<String> readStatuses,
                                    List<String> genres, Boolean isCompleted, Float ratingMin, Float ratingMax,
                                    Boolean unratedOnly, List<String> attractivePoints, List<String> keywords) {
        applyFilters(queryBuilder, isInterest, readStatuses, attractivePoints, null, null, null);

        Optional.ofNullable(genres)
                .filter(list -> !list.isEmpty())
                .ifPresent(names -> queryBuilder.where(novel.novelGenres.any().genre.genreName.in(names)));

        Optional.ofNullable(isCompleted)
                .ifPresent(completed -> queryBuilder.where(novel.isCompleted.eq(completed)));

        if (Boolean.TRUE.equals(unratedOnly)) {
            queryBuilder.where(userNovel.userNovelRating.eq(0.0f));
        } else {
            Optional.ofNullable(ratingMin)
                    .ifPresent(min -> queryBuilder.where(userNovel.userNovelRating.goe(min)));
            Optional.ofNullable(ratingMax)
                    .ifPresent(max -> queryBuilder.where(userNovel.userNovelRating.loe(max)));
        }

        Optional.ofNullable(keywords)
                .filter(list -> !list.isEmpty())
                .ifPresent(names -> queryBuilder.where(userNovel.userNovelKeywords.any().keyword.keywordName.in(names)));
    }

    private BooleanExpression cursorCondition(UserNovelCursor cursor, UserNovelSortType sortType) {
        if (cursor == null) {
            return null;
        }

        return switch (sortType) {
            case CREATED_DESC -> createdDescCursorCondition(cursor);
            case CREATED_ASC -> createdAscCursorCondition(cursor);
            case TITLE, TITLE_ASC -> titleAscCursorCondition(cursor);
            case TITLE_DESC -> titleDescCursorCondition(cursor);
            case READ_DATE -> readDateCursorCondition(cursor);
            case RATING_DESC -> ratingDescCursorCondition(cursor);
            case RATING_ASC -> ratingAscCursorCondition(cursor);
        };
    }

    private BooleanExpression createdDescCursorCondition(UserNovelCursor cursor) {
        return userNovel.createdDate.lt(cursor.lastCreatedDate())
                .or(userNovel.createdDate.eq(cursor.lastCreatedDate())
                        .and(userNovel.userNovelId.lt(cursor.lastUserNovelId())));
    }

    private BooleanExpression createdAscCursorCondition(UserNovelCursor cursor) {
        return userNovel.createdDate.gt(cursor.lastCreatedDate())
                .or(userNovel.createdDate.eq(cursor.lastCreatedDate())
                        .and(userNovel.userNovelId.gt(cursor.lastUserNovelId())));
    }

    private BooleanExpression titleAscCursorCondition(UserNovelCursor cursor) {
        if (cursor.lastTitle() == null) {
            return null;
        }

        return novel.title.gt(cursor.lastTitle())
                .or(novel.title.eq(cursor.lastTitle())
                        .and(userNovel.userNovelId.gt(cursor.lastUserNovelId())));
    }

    private BooleanExpression titleDescCursorCondition(UserNovelCursor cursor) {
        if (cursor.lastTitle() == null) {
            return null;
        }

        return novel.title.lt(cursor.lastTitle())
                .or(novel.title.eq(cursor.lastTitle())
                        .and(userNovel.userNovelId.lt(cursor.lastUserNovelId())));
    }

    private BooleanExpression readDateCursorCondition(UserNovelCursor cursor) {
        if (cursor.lastStartDate() == null) {
            return userNovel.startDate.isNull()
                    .and(userNovel.userNovelId.lt(cursor.lastUserNovelId()));
        }

        return userNovel.startDate.lt(cursor.lastStartDate())
                .or(userNovel.startDate.eq(cursor.lastStartDate())
                        .and(userNovel.userNovelId.lt(cursor.lastUserNovelId())))
                .or(userNovel.startDate.isNull());
    }

    private BooleanExpression ratingDescCursorCondition(UserNovelCursor cursor) {
        if (Boolean.TRUE.equals(cursor.rated())) {
            return userNovel.userNovelRating.ne(0.0f)
                    .and(userNovel.userNovelRating.lt(cursor.lastRating())
                            .or(userNovel.userNovelRating.eq(cursor.lastRating())
                                    .and(createdDescCursorCondition(cursor))))
                    .or(userNovel.userNovelRating.eq(0.0f));
        }

        return userNovel.userNovelRating.eq(0.0f)
                .and(createdDescCursorCondition(cursor));
    }

    private BooleanExpression ratingAscCursorCondition(UserNovelCursor cursor) {
        if (Boolean.TRUE.equals(cursor.rated())) {
            return userNovel.userNovelRating.ne(0.0f)
                    .and(userNovel.userNovelRating.gt(cursor.lastRating())
                            .or(userNovel.userNovelRating.eq(cursor.lastRating())
                                    .and(createdDescCursorCondition(cursor))));
        }

        return userNovel.userNovelRating.eq(0.0f)
                .and(createdDescCursorCondition(cursor))
                .or(userNovel.userNovelRating.ne(0.0f));
    }
}
