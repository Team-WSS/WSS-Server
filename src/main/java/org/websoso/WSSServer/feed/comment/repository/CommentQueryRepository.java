package org.websoso.WSSServer.feed.comment.repository;

import java.util.List;
import org.websoso.WSSServer.feed.comment.repository.projection.CommentInfoRow;

public interface CommentQueryRepository {

    List<CommentInfoRow> findCommentInfoRows(Long feedId, Long userId);
}
