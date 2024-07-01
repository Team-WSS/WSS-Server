package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomFeedError;

@Getter
@AllArgsConstructor
public class CustomFeedException extends RuntimeException {

    public CustomFeedException(CustomFeedError customFeedError, String message) {
        super(message);
        this.customFeedError = customFeedError;
    }

    private CustomFeedError customFeedError;

}
