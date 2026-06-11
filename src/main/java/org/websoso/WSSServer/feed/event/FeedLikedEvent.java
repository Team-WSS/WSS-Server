package org.websoso.WSSServer.feed.event;

public record FeedLikedEvent(
        Long userId,
        Long feedId,
        Long writerId
) {
    public static FeedLikedEvent of(Long userId, Long feedId, Long writerId) {
        return new FeedLikedEvent(userId, feedId, writerId);
    }
}
