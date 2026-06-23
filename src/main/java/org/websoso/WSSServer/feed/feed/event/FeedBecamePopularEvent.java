package org.websoso.WSSServer.feed.feed.event;

public record FeedBecamePopularEvent(
        Long feedId
) {
    public static FeedBecamePopularEvent of(Long feedId) {
        return new FeedBecamePopularEvent(feedId);
    }
}
