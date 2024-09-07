package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomCommentError.ALREADY_REPORTED_COMMENT;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.ReportedComment;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.repository.ReportedCommentRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportedCommentService {

    private final ReportedCommentRepository reportedCommentRepository;

    public void createReportedComment(Comment comment, User user, ReportedType reportedType) {
        if (reportedCommentRepository.existsByCommentAndUserAndReportedType(comment, user, reportedType)) {
            throw new CustomCommentException(ALREADY_REPORTED_COMMENT, "comment has already been reported by the user");
        }

        reportedCommentRepository.save(ReportedComment.create(comment, user, reportedType));
    }

    public int getReportedCountByReportedType(Comment comment, ReportedType reportedType) {
        return reportedCommentRepository.countByCommentAndReportedType(comment, reportedType);
    }

}
