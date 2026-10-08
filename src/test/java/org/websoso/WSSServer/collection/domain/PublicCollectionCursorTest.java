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

class PublicCollectionCursorTest {

    private static final LocalDateTime CREATED_DATE = LocalDateTime.of(2025, 3, 4, 5, 6, 7, 891_234_000);
    private static final long COLLECTION_ID = 42L;

    @DisplayName("커서는 생성 시점과 식별자를 모두 담고 그대로 복원된다")
    @Test
    void restoresBothCreatedDateAndId() {
        PublicCollectionCursor cursor = PublicCollectionCursor.of(CREATED_DATE, COLLECTION_ID);

        PublicCollectionCursor decoded = PublicCollectionCursor.decode(cursor.encode());

        assertThat(decoded.createdDate()).isEqualTo(CREATED_DATE);
        assertThat(decoded.collectionId()).isEqualTo(COLLECTION_ID);
    }

    @DisplayName("초 미만 정밀도까지 그대로 복원해 같은 초에 만들어진 컬렉션을 구분한다")
    @Test
    void keepsSubSecondPrecision() {
        LocalDateTime microsecond = LocalDateTime.of(2025, 3, 4, 5, 6, 7, 123_456_000);

        PublicCollectionCursor decoded = PublicCollectionCursor.decode(
                PublicCollectionCursor.of(microsecond, 1L).encode());

        assertThat(decoded.createdDate()).isEqualTo(microsecond);
    }

    @DisplayName("커서는 클라이언트가 해석할 필요가 없도록 원본 값을 그대로 노출하지 않는다")
    @Test
    void isOpaque() {
        String encoded = PublicCollectionCursor.of(CREATED_DATE, COLLECTION_ID).encode();

        assertThat(encoded).doesNotContain("2025", "|", PublicCollectionCursor.PURPOSE);
    }

    @DisplayName("커서에 전체 공개 목록 용도 표시를 담는다")
    @Test
    void carriesPurpose() {
        String encoded = PublicCollectionCursor.of(CREATED_DATE, COLLECTION_ID).encode();

        String raw = new String(Base64.getUrlDecoder().decode(encoded), UTF_8);
        assertThat(raw).startsWith(PublicCollectionCursor.PURPOSE + "|");
    }

    @DisplayName("사용자별 목록 커서는 형식이 맞아도 전체 공개 목록에서 거부한다")
    @Test
    void rejectsUserCollectionCursor() {
        String userListCursor = CollectionCursor.of(CREATED_DATE, COLLECTION_ID).encode();

        assertRejected(userListCursor);
    }

    @DisplayName("좋아요한 컬렉션 목록 커서는 형식이 맞아도 전체 공개 목록에서 거부한다")
    @Test
    void rejectsLikedCollectionCursor() {
        String likedListCursor = CollectionLikeCursor.of(CREATED_DATE, COLLECTION_ID).encode();

        assertRejected(likedListCursor);
    }

    @DisplayName("전체 공개 목록 커서는 기존 목록에서도 받아들여지지 않는다")
    @Test
    void isNotAcceptedByExistingLists() {
        String encoded = PublicCollectionCursor.of(CREATED_DATE, COLLECTION_ID).encode();

        assertThatThrownBy(() -> CollectionCursor.decode(encoded))
                .isInstanceOf(CustomCollectionException.class);
        assertThatThrownBy(() -> CollectionLikeCursor.decode(encoded))
                .isInstanceOf(CustomCollectionException.class);
    }

    @DisplayName("이 API가 발급하지 않은 커서는 요청 오류로 처리한다")
    @ParameterizedTest
    @ValueSource(strings = {"not-a-cursor", "!!!", ""})
    void rejectsCursorNotIssuedByApi(String encoded) {
        assertRejected(encoded);
    }

    @DisplayName("용도 표시나 형식이 어긋난 커서는 요청 오류로 처리한다")
    @ParameterizedTest
    @ValueSource(strings = {"public-collections|2025-03-04T05:06:07", "public-collections|2025-03-04T05:06:07|",
            "public-collections||42", "public-collections|2025-03-04T05:06:07|abc",
            "public-collections|2025-03-04T05:06:07|42|9", "public-collections|not-a-date|42",
            "user-collections|2025-03-04T05:06:07|42", "|2025-03-04T05:06:07|42"})
    void rejectsMalformedCursor(String raw) {
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(UTF_8));

        assertRejected(encoded);
    }

    private void assertRejected(String encoded) {
        assertThatThrownBy(() -> PublicCollectionCursor.decode(encoded))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_COLLECTION_CURSOR);
    }
}
