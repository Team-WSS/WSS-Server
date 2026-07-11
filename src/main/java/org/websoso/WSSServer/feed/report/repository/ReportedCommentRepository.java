package org.websoso.WSSServer.feed.report.repository;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.report.domain.ReportedComment;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.domain.common.ReportedType;

@Repository
public interface ReportedCommentRepository extends JpaRepository<ReportedComment, Long> {

    Boolean existsByCommentAndUserAndReportedType(Comment comment, User user, ReportedType reportedType);

    Integer countByCommentAndReportedType(Comment comment, ReportedType reportedType);

    @Modifying
    @Query("DELETE FROM ReportedComment rc WHERE rc.comment.commentId IN :commentIds")
    void deleteByCommentIdsIn(@Param("commentIds") List<Long> commentIds);

    @Modifying
    @Query("DELETE FROM ReportedComment rc WHERE rc.comment.feed.feedId = :feedId")
    void deleteByFeedId(Long feedId);

    void deleteByComment(Comment comment);
}
