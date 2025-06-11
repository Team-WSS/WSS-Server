package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QNovel.novel;
import static org.websoso.WSSServer.domain.QNovelGenre.novelGenre;
import static org.websoso.WSSServer.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;

import com.querydsl.core.types.Projections;
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
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;

@Repository
@RequiredArgsConstructor
public class UserNovelCustomRepositoryImpl implements UserNovelCustomRepository {

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

        queryBuilder.where(isAscending
                ? userNovel.userNovelId.gt(lastUserNovelId)
                : userNovel.userNovelId.lt(lastUserNovelId));
        queryBuilder.orderBy(isAscending
                ? userNovel.userNovelId.asc()
                : userNovel.userNovelId.desc());
        return queryBuilder.limit(size).fetch();
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
