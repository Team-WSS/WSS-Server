package org.websoso.WSSServer.repository;

import static org.websoso.WSSServer.domain.QNovel.novel;
import static org.websoso.WSSServer.domain.QNovelGenre.novelGenre;
import static org.websoso.WSSServer.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.QNovel;

@Repository
@RequiredArgsConstructor
public class NovelCustomRepositoryImpl implements NovelCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Novel> findSearchedNovels(Pageable pageable, String searchQuery) {

        BooleanExpression titleContainsQuery = getCleanedString(novel.title).containsIgnoreCase(searchQuery);
        BooleanExpression authorContainsQuery = getCleanedString(novel.author).containsIgnoreCase(searchQuery);

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

    private StringTemplate getCleanedString(StringPath stringPath) {
        return Expressions.stringTemplate(
                "CAST(REGEXP_REPLACE(REPLACE(REPLACE({0}, ' ', ''), CHAR(9), ''), '[^a-zA-Z0-9가-힣]', '') AS STRING)",
                stringPath
        );
    }

    @Override
    public Page<Novel> findFilteredNovels(Pageable pageable, List<Genre> genres, Boolean isCompleted, Float novelRating,
                                          List<Keyword> keywords) {

        NumberTemplate<Long> popularity = Expressions.numberTemplate(Long.class,
                "(SELECT COUNT(un) FROM UserNovel un WHERE un.novel = {0} AND un.isDeleted = false AND (un.isInterest = true OR un.status <> 'QUIT'))",
                novel);

        JPAQuery<Novel> query = jpaQueryFactory
                .selectFrom(novel)
                .distinct()
                .join(novel.novelGenres, novelGenre)
                .where(
                        genres.isEmpty()
                                ? null
                                : novelGenre.genre.in(genres),
                        isCompleted == null
                                ? null
                                : novel.isCompleted.eq(isCompleted),
                        novelRating == null
                                ? null
                                : getAverageRating(novel).goe(novelRating),
                        keywords.isEmpty()
                                ? null
                                : getKeywordCount(novel, keywords).eq(keywords.size())
                )
                .orderBy(popularity.desc());

        return applyPagination(pageable, query);
    }

    private NumberExpression<Double> getAverageRating(QNovel novel) {
        return Expressions.numberTemplate(Double.class,
                "(SELECT AVG(un.userNovelRating) FROM UserNovel un WHERE un.novel = {0} AND un.isDeleted = false AND un.userNovelRating <> 0)",
                novel);
    }

    private NumberExpression<Integer> getKeywordCount(QNovel novel, List<Keyword> keywords) {
        return Expressions.numberTemplate(Integer.class,
                "(SELECT COUNT(unk.keyword) FROM UserNovelKeyword unk JOIN unk.userNovel un WHERE un.novel = {0} AND un.isDeleted = false AND unk.keyword IN ({1}) GROUP BY un.novel.id)",
                novel, keywords);
    }

    private NumberExpression<Long> getPopularity(QNovel novel) {
        return new CaseBuilder()
                .when(
                        userNovel.isDeleted.isFalse()
                                .and(
                                        userNovel.isInterest.isTrue()
                                                .or(userNovel.status.in(WATCHING, WATCHED))
                                )
                )
                .then(1L)
                .otherwise(0L)
                .sum();
    }

    private Page<Novel> applyPagination(Pageable pageable, JPAQuery<Novel> query) {
        long total = query.fetchCount();
        List<Novel> results = query.offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }
}
