package org.websoso.WSSServer.feed.feed.event;

public record PopularFeedCheckEvent(
        Long feedId
) {
    public static PopularFeedCheckEvent of(Long feedId) {
        return new PopularFeedCheckEvent(feedId);
    }
}
