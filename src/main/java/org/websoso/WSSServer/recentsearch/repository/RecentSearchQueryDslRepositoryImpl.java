package org.websoso.WSSServer.recentsearch.repository;

import static org.websoso.WSSServer.recentsearch.domain.QRecentSearch.recentSearch;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.recentsearch.domain.RecentSearch;

@Repository
@RequiredArgsConstructor
public class RecentSearchQueryDslRepositoryImpl implements RecentSearchQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<RecentSearch> findByUserIdOrderBySearchedAtDesc(long userId, int size, LocalDateTime lastSearchedAt, Long lastRecentSearchId) {
        return jpaQueryFactory
                .selectFrom(recentSearch)
                .where(
                        checkOwner(userId),
                        cursorCondition(lastSearchedAt, lastRecentSearchId)
                )
                .limit(size)
                .orderBy(recentSearch.searchedAt.desc(), recentSearch.id.desc())
                .fetch();
    }

    private BooleanExpression checkOwner(long userId) {
        return recentSearch.userId.eq(userId);
    }

    private BooleanExpression cursorCondition(LocalDateTime lastSearchedAt, Long lastRecentSearchId) {
        if (lastSearchedAt == null || lastRecentSearchId == null) {
            return null;
        }
        return recentSearch.searchedAt.lt(lastSearchedAt)
                .or(recentSearch.searchedAt.eq(lastSearchedAt)
                        .and(recentSearch.id.lt(lastRecentSearchId)));
    }


}
