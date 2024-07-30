package org.websoso.WSSServer.repository;


import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.QNovel;
import org.websoso.WSSServer.domain.QNovelGenre;

@Repository
@RequiredArgsConstructor
public class NovelCustomRepositoryImpl implements NovelCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Novel> findFilteredNovels(Pageable pageable, List<Genre> genres, Boolean isCompleted, Float novelRating,
                                          List<Keyword> keywords) {

        QNovel novel = QNovel.novel;
        QNovelGenre novelGenre = QNovelGenre.novelGenre;

        NumberTemplate<Long> popularity = Expressions.numberTemplate(Long.class,
                "(SELECT COUNT(un) FROM UserNovel un WHERE un.novel = {0} AND (un.isInterest = true OR un.status <> 'QUIT'))",
                novel);

        JPAQuery<Novel> query = jpaQueryFactory.selectFrom(novel)
                .join(novel.novelGenres, novelGenre)
                .where(
                        genres.isEmpty() ? null : novelGenre.genre.in(genres),
                        isCompleted == null ? null : novel.isCompleted.eq(isCompleted),
                        novelRating == null ? null : getAverageRating(novel).goe(novelRating),
                        keywords.isEmpty() ? null : getKeywordCount(novel, keywords).eq(keywords.size())
                )
                .orderBy(popularity.desc());

        return applyPagination(pageable, query);
    }

    private NumberExpression<Double> getAverageRating(QNovel novel) {
        return Expressions.numberTemplate(Double.class,
                "(SELECT AVG(un.userNovelRating) FROM UserNovel un WHERE un.novel = {0} AND un.userNovelRating <> 0)",
                novel);
    }

    private NumberExpression<Integer> getKeywordCount(QNovel novel, List<Keyword> keywords) {
        return Expressions.numberTemplate(Integer.class,
                "(SELECT COUNT(unk.keyword) FROM UserNovelKeyword unk WHERE unk.userNovel.novel = {0} AND unk.keyword IN ({1}) GROUP BY unk.userNovel.novel.id)",
                novel, keywords);
    }

    private Page<Novel> applyPagination(Pageable pageable, JPAQuery<Novel> query) {
        long total = query.fetchCount();
        List<Novel> results = query.offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

}
