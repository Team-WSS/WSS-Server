package org.websoso.WSSServer.feed.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.JPQLQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class FeedQueryRepositoryImplTest {

    private final FeedQueryRepositoryImpl repository = new FeedQueryRepositoryImpl(null);

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
}
