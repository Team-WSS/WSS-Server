package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CategoryErrorCode;

@Getter
@AllArgsConstructor
public class CustomCategoryException extends RuntimeException {

    public CustomCategoryException(CategoryErrorCode categoryErrorCode, String message) {
        super(message);
        this.categoryErrorCode = categoryErrorCode;
    }

    private CategoryErrorCode categoryErrorCode;
}