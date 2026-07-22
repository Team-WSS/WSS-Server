package org.websoso.WSSServer.feed.comment.repository;

public interface CommentCustomRepository {

    boolean markSpoilerIfNotMarked(Long commentId);

    boolean hideIfNotHidden(Long commentId);

    void updateUserToUnknown(Long userId);

    void deleteByFeedId(Long feedId);
}
