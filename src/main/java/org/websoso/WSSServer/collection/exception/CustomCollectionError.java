package org.websoso.WSSServer.collection.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.common.exception.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomCollectionError implements ICustomError {

    COLLECTION_NOT_FOUND("COLLECTION-001", "해당 ID를 가진 컬렉션을 찾을 수 없습니다.", NOT_FOUND),
    INVALID_COLLECTION_NOVEL_COUNT("COLLECTION-002", "컬렉션에는 1개 이상 100개 이하의 작품만 포함할 수 있습니다.", BAD_REQUEST),
    DUPLICATE_COLLECTION_NOVEL("COLLECTION-003", "같은 작품을 한 컬렉션에 중복으로 포함할 수 없습니다.", BAD_REQUEST),
    REPRESENTATIVE_NOVEL_NOT_INCLUDED("COLLECTION-004", "대표 작품은 컬렉션에 포함된 작품이어야 합니다.", BAD_REQUEST),
    INVALID_AUTHORIZED_COLLECTION("COLLECTION-005", "컬렉션 소유자만 컬렉션을 수정하거나 삭제할 수 있습니다.", FORBIDDEN),
    PRIVATE_COLLECTION_ACCESS("COLLECTION-006", "비공개 컬렉션은 소유자만 조회할 수 있습니다.", FORBIDDEN),
    INVALID_COLLECTION_CURSOR("COLLECTION-007", "유효하지 않은 컬렉션 커서입니다.", BAD_REQUEST),
    INVALID_COLLECTION_PAGE_SIZE("COLLECTION-008", "컬렉션 목록은 1개 이상 100개 이하로만 조회할 수 있습니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
