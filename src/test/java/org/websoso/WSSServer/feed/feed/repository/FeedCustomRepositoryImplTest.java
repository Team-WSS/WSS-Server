package org.websoso.WSSServer.feed.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.feed.feed.domain.Feed;

/**
 * 피드 조회 조건이 작품 및 장르 필터 요구사항을 지키는지 검증한다.
 */
class FeedCustomRepositoryImplTest {

    private final FeedCustomRepositoryImpl repository =
            new FeedCustomRepositoryImpl(new JPAQueryFactory(JPQLTemplates.DEFAULT, (EntityManager) null));

    // 소설이 연결되지 않은 피드는 좋아요가 임계치를 넘어도 후보에서 빠져야 하므로 소설 조인이 내부 조인이어야 한다.
    @DisplayName("지금 뜨는 글 후보는 소설이 연결된 피드만 선택한다")
    @Test
    void selectsOnlyFeedsJoinedWithNovel() {
        String query = popularRecommendedFeedsQuery().toString();

        assertThat(query).contains("inner join Novel novel on feed.novelId = novel.novelId");
    }

    // 장르가 연결되지 않은 소설의 피드는 장르 값을 채울 수 없으므로 장르 조인도 내부 조인이어야 한다.
    @DisplayName("지금 뜨는 글 후보는 장르가 연결된 소설의 피드만 선택한다")
    @Test
    void selectsOnlyFeedsJoinedWithGenre() {
        String query = popularRecommendedFeedsQuery().toString();

        assertThat(query)
                .contains("inner join NovelGenre novelGenre on novel = novelGenre.novel")
                .contains("inner join Genre genre on novelGenre.genre = genre");
    }

    // 외부 조인이 하나라도 남으면 소설이나 장르가 없는 피드가 다시 후보로 들어온다.
    @DisplayName("지금 뜨는 글 후보 선정에는 외부 조인을 사용하지 않는다")
    @Test
    void doesNotUseLeftJoinForPopularCandidates() {
        String query = popularRecommendedFeedsQuery().toString();

        assertThat(query).doesNotContain("left join");
    }

    @DisplayName("ETC만 선택하면 작품이 연결되지 않은 피드만 조회한다")
    @Test
    void filtersFeedsWithoutNovelWhenOnlyEtcIsSelected() {
        BooleanExpression condition = ReflectionTestUtils.invokeMethod(
                repository, "checkGenresAndNovels", List.of(), true);

        assertThat(condition).isNotNull();
        assertThat(condition.toString()).isEqualTo("feed.novelId is null");
    }

    @DisplayName("장르 필터를 선택하지 않으면 작품 연결 여부를 제한하지 않는다")
    @Test
    void doesNotFilterNovelConnectionWhenNoGenreIsSelected() {
        BooleanExpression condition = ReflectionTestUtils.invokeMethod(
                repository, "checkGenresAndNovels", List.of(), false);

        assertThat(condition).isNull();
    }

    private JPAQuery<Feed> popularRecommendedFeedsQuery() {
        JPAQuery<Feed> query = ReflectionTestUtils.invokeMethod(
                repository, "popularRecommendedFeedsQuery", null, 20, List.of(), List.of());

        assertThat(query).isNotNull();
        return query;
    }
}
