package org.websoso.WSSServer.dto.feed;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record InterestFeedsGetResponse(
        List<InterestFeedGetResponse> recommendFeeds,

        @JsonInclude(NON_NULL)
        String message
) {

    public static InterestFeedsGetResponse of(List<InterestFeedGetResponse> interestFeedGetResponses) {
        return new InterestFeedsGetResponse(interestFeedGetResponses, null);
    }

    public static InterestFeedsGetResponse of(List<InterestFeedGetResponse> interestFeedGetResponses, String message) {
        return new InterestFeedsGetResponse(interestFeedGetResponses, message);
    }
}
