package org.websoso.WSSServer.dto.novel;

import java.util.List;
import org.websoso.WSSServer.feed.controller.dto.FeedInfo;

public record NovelGetResponseFeedTab(
        Boolean isLoadable,
        List<FeedInfo> feeds
) {
    public static NovelGetResponseFeedTab of(Boolean isLoadable, List<FeedInfo> feeds) {
        return new NovelGetResponseFeedTab(
                isLoadable,
                feeds
        );
    }
}
