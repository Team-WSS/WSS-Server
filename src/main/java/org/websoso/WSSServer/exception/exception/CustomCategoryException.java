package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomCategoryError;

@Getter
public class CustomCategoryException extends AbstractCustomException {

    public CustomCategoryException(CustomCategoryError customCategoryError, String message) {
        super(customCategoryError, message);
    }
}
