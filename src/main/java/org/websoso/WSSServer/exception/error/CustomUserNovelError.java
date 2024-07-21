package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@AllArgsConstructor
@Getter
public enum CustomUserNovelError implements ICustomError {

    USER_NOVEL_NOT_FOUND("USER_NOVEL-001", "해당 작품이 유저의 서재에 등록되어 있지 않습니다.", NOT_FOUND),
    USER_NOVEL_ALREADY_EXISTS("USER_NOVEL-002", "이미 서재에 등록된 작품입니다.", CONFLICT),
    ALREADY_INTERESTED("USER_NOVEL-003", "이미 해당 작품이 관심 작품으로 등록되어 있습니다.", CONFLICT),
    NOT_INTERESTED("USER_NOVEL-004", "관심으로 등록되어 있지 않은 작품입니다.", BAD_REQUEST),
    NOT_EVALUATED("USER_NOVEL-005", "평가하지 않은 작품입니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
