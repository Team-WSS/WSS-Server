package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.ReportedComment;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.ReportedType;

@Repository
public interface ReportedCommentRepository extends JpaRepository<ReportedComment, Long> {

    Boolean existsByCommentAndUserAndReportedType(Comment comment, User user, ReportedType reportedType);

    Integer countByCommentAndReportedType(Comment comment, ReportedType reportedType);

}
