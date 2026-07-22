package org.websoso.WSSServer.feed.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.report.domain.ReportedComment;
import org.websoso.WSSServer.domain.common.ReportedType;

@Repository
public interface ReportedCommentRepository extends JpaRepository<ReportedComment, Long>, ReportedCommentCustomRepository {

    int countByCommentAndReportedType(Comment comment, ReportedType reportedType);
}
