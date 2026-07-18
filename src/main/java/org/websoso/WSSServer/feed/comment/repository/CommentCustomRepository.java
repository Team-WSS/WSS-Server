package org.websoso.WSSServer.feed.comment.repository;

public interface CommentCustomRepository {

    void markSpoiler(Long commentId);

    void hide(Long commentId);

    void updateUserToUnknown(Long userId);

    void deleteByFeedId(Long feedId);
}
