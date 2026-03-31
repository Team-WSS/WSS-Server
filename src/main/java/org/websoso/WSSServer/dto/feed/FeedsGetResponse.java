package org.websoso.WSSServer.dto.feed;

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
