package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomNoticeCategoryError;

@Getter
public class CustomNoticeCategoryException extends AbstractCustomException {

    public CustomNoticeCategoryException(CustomNoticeCategoryError customNoticeCategoryError, String message) {
        super(customNoticeCategoryError, message);
    }
}
