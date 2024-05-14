package org.websoso.WSSServer.exception.category.exeption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.category.CategoryErrorCode;

@Getter
@AllArgsConstructor
public class InvalidCategoryException extends RuntimeException {

    public InvalidCategoryException(CategoryErrorCode categoryErrorCode, String message) {
        super(message);
        this.categoryErrorCode = categoryErrorCode;
    }

    private CategoryErrorCode categoryErrorCode;
}