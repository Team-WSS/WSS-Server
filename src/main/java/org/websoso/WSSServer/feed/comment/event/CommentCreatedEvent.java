package org.websoso.WSSServer.feed.comment.event;

public record CommentCreatedEvent(
        Long userId,
        Long feedId
) {
    public static CommentCreatedEvent of(Long userId, Long feedId) {
        return new CommentCreatedEvent(userId, feedId);
    }
}
