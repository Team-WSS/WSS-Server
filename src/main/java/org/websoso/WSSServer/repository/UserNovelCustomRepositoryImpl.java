package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QNovel.novel;
import static org.websoso.WSSServer.domain.QNovelGenre.novelGenre;
import static org.websoso.WSSServer.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
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

    @Override
    public List<UserNovel> findUserNovelsByNoOffsetPagination(User owner, Long lastUserNovelId, int size,
                                                              String readStatus, String sortType) {
        return jpaQueryFactory
                .selectFrom(userNovel)
                .where(
                        userNovel.user.eq(owner),
                        generateReadStatusCondition(readStatus),
                        compareFeedId(lastUserNovelId, sortType)
                )
                .orderBy(getSortOrder(sortType))
                .limit(size)
                .fetch();
    }

    private BooleanExpression compareFeedId(Long lastUserNovelId, String sortType) {
        if (lastUserNovelId == 0) {
            return null;
        }

        // TODO 잘못된 sortType이 오는 경우 default null로 return이 아닌 예외 처리
        if ("NEWEST".equalsIgnoreCase(sortType)) {
            return userNovel.userNovelId.lt(lastUserNovelId);
        } else if ("OLDEST".equalsIgnoreCase(sortType)) {
            return userNovel.userNovelId.gt(lastUserNovelId);
        }

        return null;
    }

    private BooleanExpression generateReadStatusCondition(String readStatus) {
        // TODO 잘못된 readStatus가 오는 경우 예외 처리
        if (readStatus.equals("INTEREST")) {
            return userNovel.isInterest.isTrue();
        } else {
            ReadStatus status = ReadStatus.valueOf(readStatus);
            return userNovel.status.eq(status);
        }
    }

    private OrderSpecifier<?> getSortOrder(String sortType) {
        // TODO 잘못된 sortType이 오는 경우 default desc가 아닌 예외 처리
        if ("NEWEST".equalsIgnoreCase(sortType)) {
            return userNovel.userNovelId.desc();
        } else if ("OLDEST".equalsIgnoreCase(sortType)) {
            return userNovel.userNovelId.asc();
        }
        return userNovel.userNovelId.desc();
    }

    @Override
    public List<UserNovel> findByUserAndReadStatus(User owner, String readStatus) {
        return jpaQueryFactory
                .selectFrom(userNovel)
                .where(userNovel.user.eq(owner),
                        generateReadStatusCondition(readStatus)
                )
                .fetch();
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
        JPAQuery<UserNovel> queryBuilder = jpaQueryFactory
                .selectFrom(userNovel)
                .join(userNovel.novel, novel).fetchJoin()
                .where(userNovel.user.userId.eq(userId));

        if (isInterest != null) {
            queryBuilder.where(userNovel.isInterest.eq(isInterest));
        }
        if (readStatuses != null && !readStatuses.isEmpty()) {
            List<ReadStatus> statusEnums = readStatuses.stream()
                    .map(ReadStatus::valueOf)
                    .toList();
            queryBuilder.where(userNovel.status.in(statusEnums));
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
        if (isAscending) {
            queryBuilder.where(userNovel.userNovelId.gt(lastUserNovelId));
        } else {
            queryBuilder.where(userNovel.userNovelId.lt(lastUserNovelId));
        }
        queryBuilder.orderBy(isAscending ? userNovel.userNovelId.asc() : userNovel.userNovelId.desc()).limit(size);

        return queryBuilder.fetch();
    }

    @Override
    public Long countByUserIdAndFilters(Long userId, Boolean isInterest, List<String> readStatuses,
                                        List<String> attractivePoints, Float novelRating, String query) {
        JPAQuery<Long> queryBuilder = jpaQueryFactory
                .select(userNovel.count())
                .from(userNovel)
                .join(userNovel.novel, novel)
                .where(userNovel.user.userId.eq(userId));

        if (isInterest != null) {
            queryBuilder.where(userNovel.isInterest.eq(isInterest));
        }
        if (readStatuses != null && !readStatuses.isEmpty()) {
            List<ReadStatus> statusEnums = readStatuses.stream()
                    .map(ReadStatus::valueOf)
                    .toList();
            queryBuilder.where(userNovel.status.in(statusEnums));
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
        return queryBuilder.fetchOne();
    }
}
