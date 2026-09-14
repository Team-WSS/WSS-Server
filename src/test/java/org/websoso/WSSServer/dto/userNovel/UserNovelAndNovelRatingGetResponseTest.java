package org.websoso.WSSServer.dto.userNovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelStatistics;

@ExtendWith(MockitoExtension.class)
class UserNovelAndNovelRatingGetResponseTest {

    @Mock
    private UserNovel userNovel;

    @Mock
    private Novel novel;

    @Mock
    private NovelStatistics novelStatistics;

    @DisplayName("서재 작품의 평균 평점을 통계 데이터로 반환한다")
    @Test
    void createsResponseWithNovelStatistics() {
        given(userNovel.getNovel()).willReturn(novel);
        given(novel.getNovelStatistics()).willReturn(novelStatistics);
        given(novelStatistics.getAverageRating()).willReturn(new BigDecimal("4.250"));
        given(userNovel.getUserNovelRating()).willReturn(2.5f);
        given(userNovel.getUserNovelAttractivePoints()).willReturn(List.of());
        given(userNovel.getUserNovelKeywords()).willReturn(List.of());

        UserNovelAndNovelGetResponse response = UserNovelAndNovelGetResponse.from(userNovel, List.of());

        assertThat(response.novelRating()).isEqualTo(4.3f);
        assertThat(response.userNovelRating()).isEqualTo(2.5f);
    }
}
