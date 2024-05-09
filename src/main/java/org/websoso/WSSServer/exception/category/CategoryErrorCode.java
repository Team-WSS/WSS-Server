package org.websoso.WSSServer.exception.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum CategoryErrorCode implements IErrorCode {

    INVALID_CATEGORY_FORMAT("CATEGORY-001", "카테고리 형식이 잘못되었습니다.");

    private final String code;
    private final String description;
}
