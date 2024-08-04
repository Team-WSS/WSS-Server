package org.websoso.WSSServer.dto.popularFeed;

import java.util.List;

public record PopularFeedsGetResponse(
        List<PopularFeedGetResponse> popularFeeds
) {

    public static PopularFeedsGetResponse of(List<PopularFeedGetResponse> popularFeedGetResponses) {
        return new PopularFeedsGetResponse(popularFeedGetResponses);
    }
}
