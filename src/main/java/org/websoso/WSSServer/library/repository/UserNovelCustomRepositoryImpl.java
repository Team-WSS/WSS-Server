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
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;

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
}
