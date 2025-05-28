package org.websoso.WSSServer.domain.common;

import static org.websoso.WSSServer.exception.error.CustomFilteringError.FEED_GET_OPTION_NOT_FOUND;

import org.websoso.WSSServer.exception.exception.CustomFilteringException;

public enum FeedGetOption {
    ALL,
    RECOMMENDED;

    public static FeedGetOption of(final String feedGetOption) {
        for (FeedGetOption value : FeedGetOption.values()) {
            if (value.toString().equals(feedGetOption)) {
                return value;
            }
        }
        throw new CustomFilteringException(FEED_GET_OPTION_NOT_FOUND,
                "given feed option does not exist");
    }

    public static boolean isAll(FeedGetOption feedGetOption) {
        if (feedGetOption == null) {
            return true;
        }
        return ALL.equals(feedGetOption);
    }
}
