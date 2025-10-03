package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.common.exception.ICustomError;

@AllArgsConstructor
@Getter
public enum CustomKeywordError implements ICustomError {

    KEYWORD_NOT_FOUND("KEYWORD-001", "해당 ID를 가진 키워드를 찾을 수 없습니다.", NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
