package org.websoso.WSSServer.feed.feed.controller.dto;

import java.util.List;

public record PopularFeedsGetResponse(
        List<PopularFeedGetResponse> popularFeeds
) {

    public static PopularFeedsGetResponse of(List<PopularFeedGetResponse> popularFeedGetResponses) {
        return new PopularFeedsGetResponse(popularFeedGetResponses);
    }
}
