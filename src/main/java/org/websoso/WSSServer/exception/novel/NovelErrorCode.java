package org.websoso.WSSServer.exception.novel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@Getter
@AllArgsConstructor
public enum NovelErrorCode implements IErrorCode {

    NOVEL_NOT_FOUND("NOVEL-001", "해당 ID를 가진 작품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
