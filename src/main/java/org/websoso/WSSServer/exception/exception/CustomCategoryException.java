package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomCategoryError;

@Getter
@AllArgsConstructor
public class CustomCategoryException extends RuntimeException {

    public CustomCategoryException(CustomCategoryError customCategoryError, String message) {
        super(message);
        this.customCategoryError = customCategoryError;
    }

    private CustomCategoryError customCategoryError;
}
