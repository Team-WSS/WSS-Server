package org.websoso.WSSServer.dto.userNovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.novel.domain.Novel;

/**
 * 취향 추천 작품 응답의 통계 기본값 매핑을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class TasteNovelGetResponseTest {

    @Mock
    private Novel novel;

    // 작품 통계가 없을 때 평점과 평가 수를 0으로 반환하는지 검증한다.
    @DisplayName("작품 통계가 없으면 평점 통계를 0으로 반환한다")
    @Test
    void returnsZeroRatingWhenStatisticsDoNotExist() {
        given(novel.getNovelId()).willReturn(1L);
        given(novel.getTitle()).willReturn("작품명");
        given(novel.getAuthor()).willReturn("작가명");
        given(novel.getNovelImage()).willReturn("https://example.com/novel.png");

        TasteNovelGetResponse response = TasteNovelGetResponse.of(novel, 2L);

        assertThat(response).isEqualTo(new TasteNovelGetResponse(
                1L,
                "작품명",
                "작가명",
                "https://example.com/novel.png",
                2L,
                0.0f,
                0L
        ));
    }
}
