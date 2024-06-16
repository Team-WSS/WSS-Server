package org.websoso.WSSServer.exception.block;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum BlockErrorCode implements IErrorCode {

    ALREADY_BLOCKED("BLOCK-001", "이미 차단한 계정입니다.", CONFLICT),
    SELF_BLOCKED("BLOCK-002", "본인을 차단할 수 없습니다.", BAD_REQUEST),
    BLOCK_NOT_FOUND("BLOCK-003", "해당 ID에 해당하는 차단을 찾을 수 없습니다.", NOT_FOUND),
    INVALID_BLOCK_ID("BLOCK-004", "유효하지 않은 ID입니다.", BAD_REQUEST),
    INVALID_AUTHORIZED_BLOCK("BLOCK-005", "해당 유저의 차단이 아닙니다.", FORBIDDEN),
    CANNOT_ADMIN_BLOCK("BLOCK-006", "관리자는 차단할 수 없습니다.", FORBIDDEN);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
