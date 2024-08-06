package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QNovel.novel;
import static org.websoso.WSSServer.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.QNovel;

@Repository
@RequiredArgsConstructor
public class NovelCustomRepositoryImpl implements NovelCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Novel> findSearchedNovels(Pageable pageable, String query) {

        String searchQuery = query.replaceAll("\\s+", "");

        BooleanExpression titleContainsQuery = getSpaceRemovedString(novel.title).containsIgnoreCase(searchQuery);
        BooleanExpression authorContainsQuery = getSpaceRemovedString(novel.author).containsIgnoreCase(searchQuery);

        List<Novel> novelsByTitle = jpaQueryFactory
                .selectFrom(novel)
                .leftJoin(novel.userNovels, userNovel)
                .where(titleContainsQuery)
                .groupBy(novel.novelId)
                .orderBy(getPopularity(novel).desc())
                .fetch();

        List<Novel> novelsByAuthor = jpaQueryFactory
                .selectFrom(novel)
                .leftJoin(novel.userNovels, userNovel)
                .where(authorContainsQuery.and(titleContainsQuery.not()))
                .groupBy(novel.novelId)
                .orderBy(getPopularity(novel).desc())
                .fetch();

        List<Novel> result = Stream
                .concat(novelsByTitle.stream(), novelsByAuthor.stream())
                .toList();

        long total = result.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), (int) total);

        return new PageImpl<>(result.subList(start, end), pageable, total);
    }

    private StringTemplate getSpaceRemovedString(StringPath stringPath) {
        return Expressions.stringTemplate(
                "REPLACE(REPLACE({0}, ' ', ''), CHAR(9), '')",
                stringPath
        );
    }

    private NumberTemplate<Long> getPopularity(QNovel novel) {
        return Expressions.numberTemplate(
                Long.class,
                "COUNT(userNovel.userNovelId)",
                jpaQueryFactory.selectFrom(userNovel)
                        .where(userNovel.novel.eq(novel)
                                .and(userNovel.status.ne(QUIT)
                                        .or(userNovel.isInterest.isTrue())))
        );
    }

}
