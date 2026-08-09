package org.websoso.WSSServer.collection.domain;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_CURSOR;

import java.time.LocalDateTime;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;

class CollectionLikeCursorTest {

    private static final LocalDateTime LIKED_DATE = LocalDateTime.of(2025, 3, 4, 5, 6, 7, 891_234_000);
    private static final long COLLECTION_LIKE_ID = 42L;

    @DisplayName("커서는 좋아요 시점과 좋아요 식별자를 모두 담고 그대로 복원된다")
    @Test
    void restoresBothLikedDateAndLikeId() {
        CollectionLikeCursor cursor = CollectionLikeCursor.of(LIKED_DATE, COLLECTION_LIKE_ID);

        CollectionLikeCursor decoded = CollectionLikeCursor.decode(cursor.encode());

        assertThat(decoded.likedDate()).isEqualTo(LIKED_DATE);
        assertThat(decoded.collectionLikeId()).isEqualTo(COLLECTION_LIKE_ID);
    }

    @DisplayName("초 미만 정밀도까지 그대로 복원해 같은 초에 눌린 좋아요를 구분한다")
    @Test
    void keepsSubSecondPrecision() {
        LocalDateTime microsecond = LocalDateTime.of(2025, 3, 4, 5, 6, 7, 123_456_000);

        CollectionLikeCursor decoded = CollectionLikeCursor.decode(
                CollectionLikeCursor.of(microsecond, 1L).encode());

        assertThat(decoded.likedDate()).isEqualTo(microsecond);
    }

    @DisplayName("커서는 클라이언트가 해석할 필요가 없도록 원본 값을 그대로 노출하지 않는다")
    @Test
    void isOpaque() {
        String encoded = CollectionLikeCursor.of(LIKED_DATE, COLLECTION_LIKE_ID).encode();

        assertThat(encoded).doesNotContain("2025", "|");
    }

    @DisplayName("이 API가 발급하지 않은 커서는 요청 오류로 처리한다")
    @ParameterizedTest
    @ValueSource(strings = {"not-a-cursor", "!!!", ""})
    void rejectsCursorNotIssuedByApi(String encoded) {
        assertThatThrownBy(() -> CollectionLikeCursor.decode(encoded))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_COLLECTION_CURSOR);
    }

    @DisplayName("형식이 어긋난 커서는 요청 오류로 처리한다")
    @ParameterizedTest
    @ValueSource(strings = {"2025-03-04T05:06:07", "2025-03-04T05:06:07|", "|42", "2025-03-04T05:06:07|abc",
            "2025-03-04T05:06:07|42|9", "not-a-date|42"})
    void rejectsMalformedCursor(String raw) {
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(UTF_8));

        assertThatThrownBy(() -> CollectionLikeCursor.decode(encoded))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_COLLECTION_CURSOR);
    }
}
