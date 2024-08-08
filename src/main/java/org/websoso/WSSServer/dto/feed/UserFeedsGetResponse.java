package org.websoso.WSSServer.dto.feed;

import java.util.List;

public record UserFeedsGetResponse(
        Boolean isLoadable,
        List<UserFeedGetResponse> feeds
) {

    public static UserFeedsGetResponse of(Boolean isLoadable, List<UserFeedGetResponse> userFeedGetResponses) {
        return new UserFeedsGetResponse(isLoadable, userFeedGetResponses);
    }
}
