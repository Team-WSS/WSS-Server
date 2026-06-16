package org.websoso.WSSServer.feed.exception;

import lombok.Getter;
import org.websoso.common.exception.AbstractCustomException;

@Getter
public class CustomFeedException extends AbstractCustomException {

    public CustomFeedException(CustomFeedError customFeedError, String message) {
        super(customFeedError, message);
    }
}
