package org.websoso.WSSServer.collection.domain;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_CURSOR;

import java.time.LocalDateTime;
import java.util.Base64;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;

/**
 * 전체 공개 컬렉션 목록의 커서.
 * <p>
 * 정렬 기준은 사용자별 컬렉션 목록과 같은 (최초 생성 시점, 식별자)이므로 마지막으로 읽은 컬렉션의
 * {@code createdDate}와 {@code collectionId}를 그대로 담는다. 식별자만 넘기고 기준 행을 다시 조회하지 않으므로
 * 커서로 쓰던 컬렉션이 삭제돼도 다음 페이지를 읽을 수 있다({@link CollectionCursor}와 같은 이유).
 * <p>
 * 담는 값은 {@link CollectionCursor}와 같지만 가리키는 목록이 다르다. 사용자별 목록 커서를 이 목록에 넘기면
 * 형식은 맞아도 다른 목록의 위치를 이어 읽게 되므로, 커서 앞에 용도 표시({@value #PURPOSE})를 붙여 서로 바꿔 쓸 수
 * 없게 한다. 용도 표시가 없는 기존 커서는 이 목록에서 거부되고, 이 커서는 필드 수가 달라 기존 목록에서 거부된다.
 * 기존 목록의 커서 형식은 바꾸지 않는다.
 * <p>
 * 클라이언트는 커서를 해석하지 않고 다음 요청에 그대로 돌려주기만 하면 되므로 Base64로 인코딩해 불투명하게 둔다.
 */
public record PublicCollectionCursor(LocalDateTime createdDate, Long collectionId) {

    static final String PURPOSE = "public-collections";

    private static final String DELIMITER = "|";
    private static final int FIELD_COUNT = 3;

    public static PublicCollectionCursor of(LocalDateTime createdDate, Long collectionId) {
        return new PublicCollectionCursor(createdDate, collectionId);
    }

    /**
     * 커서 문자열을 복원한다. 클라이언트가 임의로 만든 값이나 다른 목록의 커서가 들어올 수 있으므로
     * 용도 표시나 형식이 어긋나면 컬렉션 도메인 예외로 처리하고, 해석할 수 없는 값을 조회 조건으로 쓰지 않는다.
     */
    public static PublicCollectionCursor decode(String encoded) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(encoded), UTF_8);
            String[] fields = raw.split("\\" + DELIMITER, -1);
            if (fields.length != FIELD_COUNT || !PURPOSE.equals(fields[0])) {
                throw invalidCursor();
            }

            return new PublicCollectionCursor(
                    LocalDateTime.parse(fields[1], ISO_LOCAL_DATE_TIME),
                    Long.parseLong(fields[2])
            );
        } catch (CustomCollectionException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw invalidCursor();
        }
    }

    public String encode() {
        String raw = PURPOSE + DELIMITER + createdDate.format(ISO_LOCAL_DATE_TIME) + DELIMITER + collectionId;

        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(UTF_8));
    }

    private static CustomCollectionException invalidCursor() {
        return new CustomCollectionException(
                INVALID_COLLECTION_CURSOR,
                "public collection cursor is not a value issued by this API"
        );
    }
}
