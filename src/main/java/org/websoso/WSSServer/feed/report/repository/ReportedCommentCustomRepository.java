package org.websoso.WSSServer.feed.report.repository;

import java.util.List;
import org.websoso.WSSServer.feed.comment.domain.Comment;

public interface ReportedCommentCustomRepository {

    void deleteByComment(Comment comment);

    void deleteByCommentIdsIn(List<Long> commentIds);

    void deleteByFeedId(Long feedId);
}
