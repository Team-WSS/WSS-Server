package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;

@Repository
@RequiredArgsConstructor
public class UserNovelCustomRepositoryImpl implements UserNovelCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Long> findTodayPopularNovelsId(Pageable pageable) {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        return jpaQueryFactory
                .select(userNovel.novel.novelId)
                .from(userNovel)
                .where(userNovel.status.eq(ReadStatus.WATCHING)
                        .or(userNovel.status.eq(ReadStatus.WATCHED))
                        .or(userNovel.isInterest.isTrue())
                        .and(userNovel.createdDate.after(sevenDaysAgo.atStartOfDay())))
                .groupBy(userNovel.novel.novelId)
                .orderBy(userNovel.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public UserNovelCountGetResponse findUserNovelStatistics(User user) {
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
                .where(userNovel.user.eq(user))
                .fetchOne();
    }

    @Override
    public List<UserNovel> findUserNovelsByNoOffsetPagination(User owner, Long lastUserNovelId, int size,
                                                              String readStatus, String sortType) {
        return jpaQueryFactory
                .selectFrom(userNovel)
                .where(
                        userNovel.user.eq(owner),
                        generateSortTypeCondition(sortType),
                        ltFeedId(lastUserNovelId)
                )
                .orderBy(userNovel.userNovelId.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression ltFeedId(Long lastUserNovelId) {
        if (lastUserNovelId == 0) {
            return null;
        }
        return userNovel.userNovelId.lt(lastUserNovelId);
    }

    private BooleanExpression generateReadStatusCondition(String readStatus) {
        if (readStatus.equals("INTEREST")) {
            return userNovel.isInterest.isTrue();
        } else {
            ReadStatus status = ReadStatus.valueOf(readStatus);
            return userNovel.status.eq(status);
        }
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
}
