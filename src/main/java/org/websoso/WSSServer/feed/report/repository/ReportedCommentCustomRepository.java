package org.websoso.WSSServer.feed.report.repository;

import org.websoso.WSSServer.feed.comment.domain.Comment;

public interface ReportedCommentCustomRepository {

    void deleteByComment(Comment comment);

    void deleteByFeedId(Long feedId);
}
