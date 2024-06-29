package org.websoso.WSSServer.exception.userNovel;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum UserNovelErrorCode implements IErrorCode {

    USER_NOVEL_ALREADY_EXISTS("USER_NOVEL-001", "이미 서재에 등록된 작품입니다.", CONFLICT),
    INVALID_READ_STATUS("USER_NOVEL-002", "바르지 않은 읽기 상태입니다.", NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
