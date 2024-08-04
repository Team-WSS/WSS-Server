package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QNovel.novel;
import static org.websoso.WSSServer.domain.QUserNovel.userNovel;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;

@Repository
@RequiredArgsConstructor
public class NovelCustomRepositoryImpl implements NovelCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Novel> findSearchedNovels(Pageable pageable, String query) {

        if (query.isEmpty() || query.isBlank()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String searchQuery = query.replaceAll("\\s+", "");

        StringTemplate cleanTitle = Expressions.stringTemplate(
                "REPLACE(REPLACE({0}, ' ', ''), CHAR(9), '')",
                novel.title
        );
        StringTemplate cleanAuthor = Expressions.stringTemplate(
                "REPLACE(REPLACE({0}, ' ', ''), CHAR(9), '')",
                novel.author
        );

        NumberTemplate<Long> popularity = Expressions.numberTemplate(Long.class,
                "(SELECT COUNT(un) FROM UserNovel un WHERE un.novel = {0} AND (un.isInterest = true OR un.status <> 'QUIT'))",
                novel);

        BooleanExpression titleContainsQuery = cleanTitle.containsIgnoreCase(searchQuery);
        BooleanExpression authorContainsQuery = cleanAuthor.containsIgnoreCase(searchQuery);

        List<Novel> novelsByTitle = jpaQueryFactory.selectFrom(novel)
                .leftJoin(novel.userNovels, userNovel)
                .where(titleContainsQuery)
                .groupBy(novel.novelId)
                .orderBy(popularity.desc())
                .fetch();

        List<Novel> novelsByAuthor = jpaQueryFactory.selectFrom(novel)
                .leftJoin(novel.userNovels, userNovel)
                .where(authorContainsQuery.and(titleContainsQuery.not()))
                .groupBy(novel.novelId)
                .orderBy(popularity.desc())
                .fetch();

        novelsByTitle.addAll(novelsByAuthor);

        long total = novelsByTitle.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), (int) total);

        return new PageImpl<>(novelsByTitle.subList(start, end), pageable, total);
    }

}
