package org.websoso.WSSServer.exception.novel;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@Getter
@AllArgsConstructor
public enum NovelErrorCode implements IErrorCode {

    NOVEL_NOT_FOUND("NOVEL-001", "해당 ID를 가진 작품을 찾을 수 없습니다.", NOT_FOUND),
    ALREADY_INTERESTED("NOVEL-002", "이미 해당 작품이 관심 작품으로 등록되어 있습니다.", CONFLICT);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
