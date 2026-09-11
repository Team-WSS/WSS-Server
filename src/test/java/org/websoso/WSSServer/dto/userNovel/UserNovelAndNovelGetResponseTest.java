package org.websoso.WSSServer.dto.userNovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.novel.domain.Novel;

class UserNovelAndNovelGetResponseTest {

    @DisplayName("서재의 노벨 정보에 작가명을 포함한다")
    @Test
    void includesNovelAuthor() {
        UserNovel userNovel = mock(UserNovel.class);
        Novel novel = mock(Novel.class);
        given(userNovel.getNovel()).willReturn(novel);
        given(userNovel.getUserNovelAttractivePoints()).willReturn(List.of());
        given(userNovel.getUserNovelKeywords()).willReturn(List.of());
        given(novel.getAuthor()).willReturn("테스트 작가");

        UserNovelAndNovelGetResponse response = UserNovelAndNovelGetResponse.from(
                userNovel, 4.5f, List.of());

        assertThat(response.author()).isEqualTo("테스트 작가");
    }
}
