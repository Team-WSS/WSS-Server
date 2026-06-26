package org.websoso.WSSServer.feed.comment.repository;

public interface CommentCustomRepository {

    void updateUserToUnknown(Long userId);

    void deleteByFeedId(Long feedId);
}
