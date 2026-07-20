package org.websoso.WSSServer.dto.popularNovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.websoso.WSSServer.novel.domain.Novel;

class PopularNovelGetResponseTest {

    @DisplayName("노출할 추천 피드가 없으면 작품 소개글을 한마디로 반환한다")
    @Test
    void fallsBackToNovelDescriptionWithoutVisibleFeed() {
        Novel novel = org.mockito.Mockito.mock(Novel.class);
        given(novel.getNovelDescription()).willReturn("작품 소개글");

        PopularNovelGetResponse response = PopularNovelGetResponse.of(novel, null, null, List.of());

        assertThat(response.feedContent()).isEqualTo("작품 소개글");
    }
}
