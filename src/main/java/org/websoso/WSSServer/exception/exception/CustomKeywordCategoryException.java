package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomKeywordCategoryError;

@Getter
public class CustomKeywordCategoryException extends AbstractCustomException {

    public CustomKeywordCategoryException(CustomKeywordCategoryError customKeywordCategoryError, String message) {
        super(customKeywordCategoryError, message);
    }
}
