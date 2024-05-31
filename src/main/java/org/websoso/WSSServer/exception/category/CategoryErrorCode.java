package org.websoso.WSSServer.exception.category;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum CategoryErrorCode implements IErrorCode {

    INVALID_CATEGORY_FORMAT("CATEGORY-001", "카테고리 형식이 잘못되었습니다.", BAD_REQUEST),
    CATEGORY_NOT_FOUND("CATEGORY-002", "피드에 카테고리가 존재하지 않습니다.", NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
    
}
