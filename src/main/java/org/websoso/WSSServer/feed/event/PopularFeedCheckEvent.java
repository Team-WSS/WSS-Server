package org.websoso.WSSServer.feed.event;

public record PopularFeedCheckEvent(
        Long feedId
) {
    public static PopularFeedCheckEvent of(Long feedId) {
        return new PopularFeedCheckEvent(feedId);
    }
}
