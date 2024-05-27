package org.websoso.WSSServer.exception.feed.exeption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.feed.FeedErrorCode;

@Getter
@AllArgsConstructor
public class InvalidFeedException extends RuntimeException {

    public InvalidFeedException(FeedErrorCode feedErrorCode, String message) {
        super(message);
        this.feedErrorCode = feedErrorCode;
    }

    private FeedErrorCode feedErrorCode;
}
