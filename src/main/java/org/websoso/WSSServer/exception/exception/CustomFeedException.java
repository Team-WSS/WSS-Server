package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.common.exception.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomFeedError;

@Getter
public class CustomFeedException extends AbstractCustomException {

    public CustomFeedException(CustomFeedError customFeedError, String message) {
        super(customFeedError, message);
    }
}
