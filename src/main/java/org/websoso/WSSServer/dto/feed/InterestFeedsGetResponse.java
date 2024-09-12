package org.websoso.WSSServer.dto.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

public record InterestFeedsGetResponse(
        List<InterestFeedGetResponse> recommendFeeds,

        @JsonInclude(Include.NON_NULL)
        String message
) {

    public static InterestFeedsGetResponse of(List<InterestFeedGetResponse> interestFeedGetResponses) {
        return new InterestFeedsGetResponse(interestFeedGetResponses, null);
    }

    public static InterestFeedsGetResponse of(List<InterestFeedGetResponse> interestFeedGetResponses, String message) {
        return new InterestFeedsGetResponse(interestFeedGetResponses, message);
    }
}
