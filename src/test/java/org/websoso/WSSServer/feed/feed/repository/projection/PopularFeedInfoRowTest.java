package org.websoso.WSSServer.feed.feed.repository.projection;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.websoso.WSSServer.feed.feed.controller.dto.PopularFeedGetResponse;

/**
 * 지금 뜨는 글 조회 결과가 응답으로 변환될 때 소설 정보가 그대로 전달되는지 검증한다.
 */
class PopularFeedInfoRowTest {

    private static final String NOVEL_TITLE = "재혼 황후";
    private static final String NOVEL_IMAGE = "https://image.websoso.kr/novel/1.png";
    private static final String NOVEL_GENRE = "romance";

    // 조회한 소설 제목, 이미지, 장르가 응답까지 값 그대로 전달되어야 한다.
    @DisplayName("지금 뜨는 글 응답의 소설 제목, 이미지, 장르는 조회 값을 그대로 매핑한다")
    @Test
    void mapsNovelTitleImageAndGenreAsIs() {
        PopularFeedGetResponse response = createRow().toResponse();

        assertThat(response.novelTitle()).isEqualTo(NOVEL_TITLE);
        assertThat(response.novelImage()).isEqualTo(NOVEL_IMAGE);
        assertThat(response.novelGenre()).isEqualTo(NOVEL_GENRE);
    }

    // 소설 정보를 빈 문자열이나 기본값으로 대체하지 않고 non-null 값으로 응답해야 한다.
    @DisplayName("지금 뜨는 글 응답의 소설 제목, 이미지, 장르는 비어 있지 않다")
    @Test
    void mapsNovelInfoWithoutNullOrBlank() {
        PopularFeedGetResponse response = createRow().toResponse();

        assertThat(response.novelTitle()).isNotNull().isNotBlank();
        assertThat(response.novelImage()).isNotNull().isNotBlank();
        assertThat(response.novelGenre()).isNotNull().isNotBlank();
    }

    // 소설 정보 외 나머지 필드도 함께 전달되는지 확인한다.
    @DisplayName("지금 뜨는 글 응답은 피드 정보와 좋아요, 댓글 수를 함께 전달한다")
    @Test
    void mapsFeedInfoWithCounts() {
        PopularFeedGetResponse response = createRow().toResponse();

        assertThat(response.feedId()).isEqualTo(1L);
        assertThat(response.feedContent()).isEqualTo("피드 내용");
        assertThat(response.likeCount()).isEqualTo(7);
        assertThat(response.commentCount()).isEqualTo(3);
        assertThat(response.isSpoiler()).isFalse();
        assertThat(response.isPublic()).isTrue();
    }

    // 소설과 장르가 연결된 피드에서 조회되는 정상 결과를 만든다.
    private PopularFeedInfoRow createRow() {
        return new PopularFeedInfoRow(
                1L,
                "피드 내용",
                7L,
                3L,
                false,
                true,
                NOVEL_TITLE,
                NOVEL_IMAGE,
                NOVEL_GENRE
        );
    }
}
