package org.websoso.WSSServer.feed.controller.dto;

import java.util.List;

public record UserFeedsGetResponse(
        Boolean isLoadable,
        long feedsCount,
        List<UserFeedGetResponse> feeds
) {

    public static UserFeedsGetResponse of(Boolean isLoadable, long feedsCount,
                                          List<UserFeedGetResponse> userFeedGetResponses) {
        return new UserFeedsGetResponse(isLoadable, feedsCount, userFeedGetResponses);
    }
}
