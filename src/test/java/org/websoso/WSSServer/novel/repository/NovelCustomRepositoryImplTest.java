package org.websoso.WSSServer.novel.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.library.domain.Keyword;

class NovelCustomRepositoryImplTest {

    private final NovelCustomRepositoryImpl repository =
            new NovelCustomRepositoryImpl(new JPAQueryFactory(JPQLTemplates.DEFAULT, (EntityManager) null));

    @DisplayName("선택한 키워드 중 하나라도 연결된 작품을 조회한다")
    @Test
    void filtersNovelsMatchingAnySelectedKeyword() {
        List<Keyword> keywords = List.of(mock(Keyword.class), mock(Keyword.class));

        BooleanExpression condition = ReflectionTestUtils.invokeMethod(repository, "hasAnyKeyword", keywords);

        assertThat(condition).isNotNull();
        String query = new JPAQueryFactory(JPQLTemplates.DEFAULT, (EntityManager) null)
                .selectFrom(novel)
                .where(condition)
                .toString();

        assertThat(query)
                .contains("exists", "userNovelKeyword.keyword in")
                .doesNotContain("count");
    }
}
