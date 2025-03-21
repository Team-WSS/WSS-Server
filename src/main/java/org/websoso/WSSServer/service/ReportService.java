package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;
import static org.websoso.WSSServer.exception.error.CustomCommentError.ALREADY_REPORTED_COMMENT;
import static org.websoso.WSSServer.exception.error.CustomFeedError.ALREADY_REPORTED_FEED;
import static org.websoso.WSSServer.exception.error.CustomFeedError.SELF_REPORT_NOT_ALLOWED;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.ReportedComment;
import org.websoso.WSSServer.domain.ReportedFeed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.exception.error.CustomCommentError;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.repository.ReportedCommentRepository;
import org.websoso.WSSServer.repository.ReportedFeedRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportedFeedRepository reportedFeedRepository;
    private final ReportedCommentRepository reportedCommentRepository;

    public int reportFeed(User reportingUser, Feed feed, ReportedType reportedType) {
        if (isUserFeedOwner(feed.getUser(), reportingUser)) {
            throw new CustomFeedException(SELF_REPORT_NOT_ALLOWED, "cannot report own feed");
        }

        createReportedFeed(feed, reportingUser, reportedType);

        int reportedCount = getReportedCountByReportedType(feed, reportedType);
        if (reportedType.isExceedingLimit(reportedCount)) {
            if (reportedType.isExceedingLimit(reportedCount)) {
                if (reportedType.equals(SPOILER)) {
                    feed.spoiler();
                } else if (reportedType.equals(IMPERTINENCE)) {
                    feed.hide();
                }
            }
        }

        return reportedCount;
    }

    public int reportComment(User reportingUser, Feed feed, Comment comment, User commentCreatedUser,
                             ReportedType reportedType) {
        comment.validateFeedAssociation(feed);

        if (isUserCommentOwner(reportingUser, commentCreatedUser)) {
            throw new CustomCommentException(CustomCommentError.SELF_REPORT_NOT_ALLOWED, "cannot report own comment");
        }

        createReportedComment(comment, reportingUser, reportedType);

        int reportedCount = getReportedCountByReportedType(comment, reportedType);
        if (reportedType.isExceedingLimit(reportedCount)) {
            if (reportedType.equals(SPOILER)) {
                comment.spoiler();
            } else if (reportedType.equals(IMPERTINENCE)) {
                comment.hideComment();
            }
        }

        return reportedCount;
    }

    private Boolean isUserFeedOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private Boolean isUserCommentOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private void createReportedFeed(Feed feed, User user, ReportedType reportedType) {
        if (reportedFeedRepository.existsByFeedAndUserAndReportedType(feed, user, reportedType)) {
            throw new CustomFeedException(ALREADY_REPORTED_FEED, "feed has already been reported by the user");
        }

        reportedFeedRepository.save(ReportedFeed.create(feed, user, reportedType));
    }

    private void createReportedComment(Comment comment, User user, ReportedType reportedType) {
        if (reportedCommentRepository.existsByCommentAndUserAndReportedType(comment, user, reportedType)) {
            throw new CustomCommentException(ALREADY_REPORTED_COMMENT, "comment has already been reported by the user");
        }

        reportedCommentRepository.save(ReportedComment.create(comment, user, reportedType));
    }

    private int getReportedCountByReportedType(Feed feed, ReportedType reportedType) {
        return reportedFeedRepository.countByFeedAndReportedType(feed, reportedType);
    }

    private int getReportedCountByReportedType(Comment comment, ReportedType reportedType) {
        return reportedCommentRepository.countByCommentAndReportedType(comment, reportedType);
    }

}
