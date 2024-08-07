package org.websoso.WSSServer.dto.feed;

import java.util.List;

public record UserFeedsGetResponse(
        Boolean isLoadable,
        List<UserFeedGetResponse> feeds
) {
}
