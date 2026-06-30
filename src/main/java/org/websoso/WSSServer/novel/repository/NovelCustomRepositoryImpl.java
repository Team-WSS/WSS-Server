package org.websoso.WSSServer.novel.repository;

import static org.websoso.WSSServer.domain.QGenre.genre;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;
import static org.websoso.WSSServer.library.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;
import static org.websoso.WSSServer.novel.domain.QNovelGenre.novelGenre;
import static org.websoso.WSSServer.novel.domain.QNovelPlatform.novelPlatform;
import static org.websoso.WSSServer.novel.domain.QPlatform.platform;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.QNovel;

@Repository
@RequiredArgsConstructor
public class NovelCustomRepositoryImpl implements NovelCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Novel> findSearchedNovels(Pageable pageable, String searchQuery) {

        BooleanExpression authorContainsQuery = getCleanedString(novel.author).containsIgnoreCase(searchQuery);

        List<Novel> novelsByTitle = jpaQueryFactory
                .selectFrom(novel)
                .leftJoin(novel.userNovels, userNovel)
                .where(titleContainsQuery(searchQuery))
                .groupBy(novel.novelId)
                .orderBy(getPopularity(novel).desc())
                .fetch();

        List<Novel> novelsByAuthor = jpaQueryFactory
                .selectFrom(novel)
                .leftJoin(novel.userNovels, userNovel)
                .where(authorContainsQuery.and(titleContainsQuery(searchQuery).not()))
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
    public Page<Novel> findFilteredNovels(Pageable pageable, List<Genre> genres, Boolean isCompleted, Float novelRatingStart,
                                          Float novelRatingEnd, List<Keyword> keywords, List<String> platformNames) {

        JPAQuery<Novel> query = jpaQueryFactory
                .selectFrom(novel);

        boolean hasGenreFilter = genres != null && !genres.isEmpty();
        boolean hasPlatformFilter = platformNames != null && !platformNames.isEmpty();

        if (hasGenreFilter) {
            query.join(novel.novelGenres, novelGenre);
        }

        if (hasPlatformFilter) {
            query.join(novel.novelPlatforms, novelPlatform)
                    .join(novelPlatform.platform, platform);
        }

        if (hasGenreFilter || hasPlatformFilter) {
            query.distinct();
        }

        query
                .where(
                        hasGenreFilter
                                ? novelGenre.genre.in(genres)
                                : null,
                        isCompleted == null
                                ? null
                                : novel.isCompleted.eq(isCompleted),
                        getAverageRatingCondition(novelRatingStart, novelRatingEnd),
                        keywords.isEmpty()
                                ? null
                                : getKeywordCount(novel, keywords).eq(keywords.size()),
                        hasPlatformFilter
                                ? platform.platformName.in(platformNames)
                                : null

                )
                .orderBy(novel.popularity.desc());

        return applyPagination(pageable, query);
    }

    @Override
    public List<Novel> findAutocompleteNovels(String searchQuery, int limitSize) {
        return jpaQueryFactory
                .selectFrom(novel)
                .leftJoin(novel.userNovels, userNovel)
                .where(titleContainsQuery(searchQuery))
                .groupBy(novel.novelId)
                .orderBy(getPopularity(novel).desc())
                .limit(limitSize)
                .fetch();
    }

    @Override
    public List<Novel> findAllByNovelIdInWithGenres(List<Long> novelIds) {
        return jpaQueryFactory
                .selectDistinct(novel)
                .from(novel)
                .leftJoin(novel.novelGenres, novelGenre).fetchJoin()
                .leftJoin(novelGenre.genre, genre).fetchJoin()
                .where(novel.novelId.in(novelIds))
                .fetch();
    }

    private BooleanExpression titleContainsQuery(String searchQuery) {
        return getCleanedString(novel.title).containsIgnoreCase(searchQuery);
    }


    private BooleanExpression getAverageRatingCondition(Float novelRatingStart, Float novelRatingEnd) {
        if (novelRatingStart == null || novelRatingEnd == null) {
            return null;
        }

        if (Float.compare(novelRatingStart, 0.0f) == 0
                && Float.compare(novelRatingEnd, 5.0f) == 0) {
            return null;
        }

        return novel.averageRating.between(
                new BigDecimal(Float.toString(novelRatingStart)),
                new BigDecimal(Float.toString(novelRatingEnd))
        );
    }

    private NumberExpression<Integer> getKeywordCount(QNovel novel, List<Keyword> keywords) {
        return Expressions.numberTemplate(Integer.class,
                "(SELECT COUNT(unk.keyword) FROM UserNovelKeyword unk WHERE unk.userNovel.novel = {0} AND unk.keyword IN ({1}) GROUP BY unk.userNovel.novel.id)",
                novel, keywords);
    }

    private NumberExpression<Long> getPopularity(QNovel novel) {
        return new CaseBuilder()
                .when(userNovel.isInterest.isTrue()
                        .or(userNovel.status.in(WATCHING, WATCHED)))
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
