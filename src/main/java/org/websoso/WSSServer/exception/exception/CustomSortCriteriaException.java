package org.websoso.WSSServer.exception.exception;

import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomSortCriteriaError;

public class CustomSortCriteriaException extends AbstractCustomException {

    public CustomSortCriteriaException(CustomSortCriteriaError customSortCriteriaError, String message) {
        super(customSortCriteriaError, message);
    }
}
