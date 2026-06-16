package org.websoso.WSSServer.feed.controller.dto;

import java.util.List;

public record FeedsGetResponse(
        Boolean isLoadable,
        List<FeedInfo> feeds
) {
    static public FeedsGetResponse of( Boolean isLoadable, List<FeedInfo> feeds) {
        return new FeedsGetResponse(
                isLoadable,
                feeds
        );
    }
}
