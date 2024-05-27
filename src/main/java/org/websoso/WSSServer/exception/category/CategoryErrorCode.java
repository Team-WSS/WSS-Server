package org.websoso.WSSServer.exception.category;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum CategoryErrorCode implements IErrorCode {

    INVALID_CATEGORY_FORMAT("CATEGORY-001", "카테고리 형식이 잘못되었습니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
    
}
