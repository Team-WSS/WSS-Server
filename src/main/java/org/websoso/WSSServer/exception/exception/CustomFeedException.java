package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.FeedErrorCode;

@Getter
@AllArgsConstructor
public class CustomFeedException extends RuntimeException {

    public CustomFeedException(FeedErrorCode feedErrorCode, String message) {
        super(message);
        this.feedErrorCode = feedErrorCode;
    }

    private FeedErrorCode feedErrorCode;

}
