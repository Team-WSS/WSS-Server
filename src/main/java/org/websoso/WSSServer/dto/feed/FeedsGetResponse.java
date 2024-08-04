package org.websoso.WSSServer.dto.feed;

import java.util.List;

public record FeedsGetResponse(
        String category,
        Boolean isLoadable,
        List<FeedInfo> feeds
) {
    static public FeedsGetResponse of(String category, Boolean isLoadable, List<FeedInfo> feeds) {
        return new FeedsGetResponse(
                category,
                isLoadable,
                feeds
        );
    }
}
