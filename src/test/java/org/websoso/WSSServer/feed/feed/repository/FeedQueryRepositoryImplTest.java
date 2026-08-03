package org.websoso.WSSServer.feed.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.feed.feed.repository.projection.PopularFeedInfoRow;

class FeedQueryRepositoryImplTest {

    private final FeedQueryRepositoryImpl repository = new FeedQueryRepositoryImpl(null);

    private final FeedQueryRepositoryImpl queryingRepository =
            new FeedQueryRepositoryImpl(new JPAQueryFactory(JPQLTemplates.DEFAULT, (EntityManager) null));

    @DisplayName("피드 댓글 수에서 사용자의 양방향 차단 관계 댓글을 제외한다")
    @Test
    void excludesBlockRelationsFromCommentCount() {
        JPQLQuery<Long> query = ReflectionTestUtils.invokeMethod(repository, "commentCount", 1L);

        assertThat(query).isNotNull();
        assertThat(query.toString())
                .contains("commentCountBlockingRelation", "commentCountBlockedRelation");
    }

    @DisplayName("비로그인 피드 댓글 수에는 차단 관계 조건을 적용하지 않는다")
    @Test
    void skipsBlockConditionForAnonymousCommentCount() {
        JPQLQuery<Long> query = ReflectionTestUtils.invokeMethod(repository, "commentCount", (Long) null);

        assertThat(query).isNotNull();
        assertThat(query.toString())
                .doesNotContain("commentCountBlockingRelation", "commentCountBlockedRelation");
    }

    // 소설이 없는 피드가 조회되면 소설 제목과 이미지가 비어서 응답에 나가므로 내부 조인으로 막는다.
    @DisplayName("지금 뜨는 글 조회는 소설이 연결된 피드만 대상으로 한다")
    @Test
    void selectsPopularFeedRowsJoinedWithNovel() {
        String query = popularFeedInfoRowsQuery();

        assertThat(query).contains("inner join");
        assertThat(query).doesNotContain("left join");
    }

    // 장르가 없는 소설의 피드가 조회되면 장르가 비어서 응답에 나가므로 존재 조건으로 막는다.
    @DisplayName("지금 뜨는 글 조회는 장르가 연결된 소설의 피드만 대상으로 한다")
    @Test
    void selectsPopularFeedRowsHavingGenre() {
        String query = popularFeedInfoRowsQuery();

        assertThat(query).contains("genreExistsSub", "genreExistsGenreSub", "exists");
    }

    // 응답의 소설 제목, 이미지, 장르는 모두 실제 조회 값으로 채워져야 한다.
    @DisplayName("지금 뜨는 글 조회는 소설 제목, 이미지, 장르를 실제 값으로 조회한다")
    @Test
    void selectsNovelTitleImageAndGenre() {
        String query = popularFeedInfoRowsQuery();

        assertThat(query).contains("novel.title", "novel.novelImage", "genreSub.genreName");
    }

    private String popularFeedInfoRowsQuery() {
        JPAQuery<PopularFeedInfoRow> query = ReflectionTestUtils.invokeMethod(
                queryingRepository, "popularFeedInfoRowsQuery", List.of(1L), 1L);

        assertThat(query).isNotNull();
        return query.toString();
    }
}
