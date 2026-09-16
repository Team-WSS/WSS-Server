package org.websoso.WSSServer.collection.domain;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_CURSOR;

import java.time.LocalDateTime;
import java.util.Base64;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;

/**
 * 사용자별 컬렉션 목록의 커서.
 * <p>
 * 최초 생성 시점({@code createdDate})과 식별자({@code collectionId})를 함께 담는다. 같은 시각에 만들어진
 * 컬렉션이 있어도 식별자가 순서를 결정적으로 정하므로, 페이지 사이에 컬렉션이 추가·삭제되어도 이미 본 항목이
 * 다시 나오거나 아직 못 본 항목이 건너뛰어지지 않는다.
 * <p>
 * 두 값을 커서 문자열 안에 그대로 담는다. 식별자만 넘기고 서버가 그 컬렉션의 생성 시각을 다시 조회하는 방식은
 * 커서로 쓰던 컬렉션이 삭제되면 기준 시각을 잃어 페이지네이션이 깨진다.
 * <p>
 * 클라이언트는 커서를 해석하지 않고 다음 요청에 그대로 돌려주기만 하면 되므로 Base64로 인코딩해 불투명하게 둔다.
 */
public record CollectionCursor(LocalDateTime createdDate, Long collectionId) {

    private static final String DELIMITER = "|";
    private static final int FIELD_COUNT = 2;

    public static CollectionCursor of(LocalDateTime createdDate, Long collectionId) {
        return new CollectionCursor(createdDate, collectionId);
    }

    /**
     * 커서 문자열을 복원한다. 클라이언트가 임의로 만든 값이 들어올 수 있으므로 형식이 어긋나면
     * 컬렉션 도메인 예외로 처리하고, 해석할 수 없는 값을 조회 조건으로 쓰지 않는다.
     */
    public static CollectionCursor decode(String encoded) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(encoded), UTF_8);
            String[] fields = raw.split("\\" + DELIMITER, -1);
            if (fields.length != FIELD_COUNT) {
                throw invalidCursor();
            }

            return new CollectionCursor(
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
        String raw = createdDate.format(ISO_LOCAL_DATE_TIME) + DELIMITER + collectionId;

        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(UTF_8));
    }

    private static CustomCollectionException invalidCursor() {
        return new CustomCollectionException(
                INVALID_COLLECTION_CURSOR,
                "collection cursor is not a value issued by this API"
        );
    }
}
