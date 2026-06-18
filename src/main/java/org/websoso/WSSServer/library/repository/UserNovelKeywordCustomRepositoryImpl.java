package org.websoso.WSSServer.library.repository;

import static org.websoso.WSSServer.library.domain.QUserNovelKeyword.userNovelKeyword;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.domain.UserNovel;

@Repository
@RequiredArgsConstructor
public class UserNovelKeywordCustomRepositoryImpl implements UserNovelKeywordCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Keyword> findTopKeywordsByCount(Pageable pageable) {
        return jpaQueryFactory
                .select(userNovelKeyword.keyword)
                .from(userNovelKeyword)
                .groupBy(userNovelKeyword.keyword)
                .orderBy(userNovelKeyword.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Keyword> findKeywordsByUserIdOrderByCountDesc(Long userId) {
        return jpaQueryFactory
                .select(userNovelKeyword.keyword)
                .from(userNovelKeyword)
                .where(userNovelKeyword.userNovel.user.userId.eq(userId))
                .groupBy(userNovelKeyword.keyword)
                .orderBy(userNovelKeyword.count().desc(), userNovelKeyword.keyword.sortOrder.asc())
                .fetch();
    }

    @Override
    public void deleteByKeywordsAndUserNovel(Set<Keyword> keywords, UserNovel userNovel) {
        jpaQueryFactory
                .delete(userNovelKeyword)
                .where(
                        userNovelKeyword.userNovel.eq(userNovel),
                        userNovelKeyword.keyword.in(keywords)
                )
                .execute();
    }
}
