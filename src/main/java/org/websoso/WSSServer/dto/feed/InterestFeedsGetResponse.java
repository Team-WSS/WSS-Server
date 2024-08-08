package org.websoso.WSSServer.dto.feed;

import java.util.List;

public record InterestFeedsGetResponse(
        List<InterestFeedGetResponse> recommendFeeds
) {

    public static InterestFeedsGetResponse of(List<InterestFeedGetResponse> interestFeedGetResponses) {
        return new InterestFeedsGetResponse(interestFeedGetResponses);
    }
}
