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
import java.util.List;
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
                                                  Long lastUserNovelId, int size, boolean isAscending) {
        JPAQuery<UserNovel> queryBuilder = buildFilterBaseQuery(userId, isInterest, readStatuses, attractivePoints,
                novelRating, query);
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
                                        List<String> attractivePoints, Float novelRating, String query) {
        return buildFilterBaseQuery(userId, isInterest, readStatuses, attractivePoints, novelRating, query)
                .select(userNovel.count())
                .fetchOne();
    }

    private JPAQuery<UserNovel> buildFilterBaseQuery(Long userId, Boolean isInterest, List<String> readStatuses,
                                                     List<String> attractivePoints, Float novelRating, String query) {
        JPAQuery<UserNovel> queryBuilder = jpaQueryFactory
                .selectFrom(userNovel)
                .join(userNovel.novel, novel).fetchJoin()
                .where(userNovel.user.userId.eq(userId));

        if (isInterest != null) {
            queryBuilder.where(userNovel.isInterest.eq(isInterest));
        }
        if (readStatuses != null && !readStatuses.isEmpty()) {
            queryBuilder.where(userNovel.status.in(readStatuses.stream().map(ReadStatus::valueOf).toList()));
        }
        if (attractivePoints != null && !attractivePoints.isEmpty()) {
            queryBuilder.where(
                    userNovel.userNovelAttractivePoints.any().attractivePoint.attractivePointName.in(attractivePoints));
        }
        if (novelRating != null) {
            queryBuilder.where(userNovel.userNovelRating.goe(novelRating));
        }
        if (query != null && !query.isBlank()) {
            queryBuilder.where(novel.title.containsIgnoreCase(query).or(novel.author.containsIgnoreCase(query)));
        }

        return queryBuilder;
    }
}
