package org.websoso.WSSServer.collection.domain;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_CURSOR;

import java.time.LocalDateTime;
import java.util.Base64;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;

/**
 * 좋아요한 컬렉션 목록의 커서.
 * <p>
 * 좋아요를 누른 시점({@code likedDate})과 좋아요 식별자({@code collectionLikeId})를 함께 담는다.
 * 목록이 컬렉션이 아니라 좋아요를 누른 순서로 정렬되므로, 커서도 컬렉션이 아니라 좋아요 행을 가리킨다.
 * 같은 시각에 눌린 좋아요가 있어도 식별자가 순서를 결정적으로 정하므로 중복과 누락이 생기지 않는다.
 * <p>
 * 두 값을 커서 안에 그대로 담는 이유는 {@link CollectionCursor}와 같다. 식별자만 넘기고 서버가 시점을
 * 다시 조회하면 커서로 쓰던 좋아요가 취소됐을 때 기준 시각을 잃어 페이지네이션이 깨진다.
 * <p>
 * 클라이언트는 커서를 해석하지 않고 다음 요청에 그대로 돌려주기만 하면 되므로 Base64로 인코딩해 불투명하게 둔다.
 */
public record CollectionLikeCursor(LocalDateTime likedDate, Long collectionLikeId) {

    private static final String DELIMITER = "|";
    private static final int FIELD_COUNT = 2;

    public static CollectionLikeCursor of(LocalDateTime likedDate, Long collectionLikeId) {
        return new CollectionLikeCursor(likedDate, collectionLikeId);
    }

    /**
     * 커서 문자열을 복원한다. 클라이언트가 임의로 만든 값이 들어올 수 있으므로 형식이 어긋나면
     * 컬렉션 도메인 예외로 처리하고, 해석할 수 없는 값을 조회 조건으로 쓰지 않는다.
     */
    public static CollectionLikeCursor decode(String encoded) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(encoded), UTF_8);
            String[] fields = raw.split("\\" + DELIMITER, -1);
            if (fields.length != FIELD_COUNT) {
                throw invalidCursor();
            }

            return new CollectionLikeCursor(
                    LocalDateTime.parse(fields[0], ISO_LOCAL_DATE_TIME),
                    Long.parseLong(fields[1])
            );
        } catch (CustomCollectionException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw invalidCursor();
        }
    }

    public String encode() {
        String raw = likedDate.format(ISO_LOCAL_DATE_TIME) + DELIMITER + collectionLikeId;

        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(UTF_8));
    }

    private static CustomCollectionException invalidCursor() {
        return new CustomCollectionException(
                INVALID_COLLECTION_CURSOR,
                "collection like cursor is not a value issued by this API"
        );
    }
}
