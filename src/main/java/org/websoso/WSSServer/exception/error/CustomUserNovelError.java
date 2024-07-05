package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@AllArgsConstructor
@Getter
public enum CustomUserNovelError implements ICustomError {

    USER_NOVEL_ALREADY_EXISTS("USER_NOVEL-001", "이미 서재에 등록된 작품입니다.", CONFLICT),
    INVALID_READ_STATUS("USER_NOVEL-002", "바르지 않은 읽기 상태입니다.", NOT_FOUND),
    ALREADY_INTERESTED("USER_NOVEL-003", "이미 해당 작품이 관심 작품으로 등록되어 있습니다.", CONFLICT);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
