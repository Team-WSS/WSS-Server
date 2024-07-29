package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;

@Repository
@RequiredArgsConstructor
public class UserNovelCustomRepositoryImpl implements UserNovelCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public UserNovelCountGetResponse findUserNovelStatistics(User user) {
        return jpaQueryFactory
                .select(Projections.constructor(UserNovelCountGetResponse.class,
                        userNovel.isInterest
                                .when(true)
                                .then(1)
                                .otherwise(0)
                                .sum(),
                        userNovel.status
                                .when(WATCHING)
                                .then(1)
                                .otherwise(0)
                                .sum(),
                        userNovel.status
                                .when(WATCHED)
                                .then(1)
                                .otherwise(0)
                                .sum(),
                        userNovel.status
                                .when(QUIT)
                                .then(1)
                                .otherwise(0)
                                .sum()
                ))
                .from(userNovel)
                .where(userNovel.user.eq(user))
                .fetchOne();
    }
}
