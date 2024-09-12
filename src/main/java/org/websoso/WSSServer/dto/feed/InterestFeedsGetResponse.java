package org.websoso.WSSServer.dto.feed;

import java.util.List;

public record InterestFeedsGetResponse(
        List<InterestFeedGetResponse> recommendFeeds,
        String message
) {

    public static InterestFeedsGetResponse of(List<InterestFeedGetResponse> interestFeedGetResponses) {
        return new InterestFeedsGetResponse(interestFeedGetResponses, null);
    }

    public static InterestFeedsGetResponse of(List<InterestFeedGetResponse> interestFeedGetResponses, String message) {
        return new InterestFeedsGetResponse(interestFeedGetResponses, message);
    }
}
