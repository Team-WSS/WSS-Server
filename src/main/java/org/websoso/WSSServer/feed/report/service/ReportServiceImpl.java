package org.websoso.WSSServer.feed.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.report.domain.ReportedComment;
import org.websoso.WSSServer.feed.report.domain.ReportedFeed;
import org.websoso.WSSServer.feed.report.repository.ReportedCommentRepository;
import org.websoso.WSSServer.feed.report.repository.ReportedFeedRepository;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl {

    private final ReportedCommentRepository reportedCommentRepository;
    private final ReportedFeedRepository reportedFeedRepository;

    @Transactional(readOnly = true)
    public Boolean isExistsByCommentAndUserAndReportedType(Comment comment, User user, ReportedType reportedType) {
        return reportedCommentRepository.existsByCommentAndUserAndReportedType(comment, user, reportedType);
    }

    @Transactional
    public void saveReportedComment(Comment comment, User user, ReportedType reportedType) {
        reportedCommentRepository.save(ReportedComment.create(comment, user, reportedType));
    }

    @Transactional(readOnly = true)
    public int countByCommentAndReportedType(Comment comment, ReportedType reportedType) {
        return reportedCommentRepository.countByCommentAndReportedType(comment, reportedType);
    }

    @Transactional(readOnly = true)
    public Boolean isExistsByFeedAndUserAndReportedType(Feed feed, User user, ReportedType reportedType) {
        return reportedFeedRepository.existsByFeedAndUserAndReportedType(feed, user, reportedType);
    }

    @Transactional
    public void saveReportedFeed(Feed feed, User user, ReportedType reportedType) {
        reportedFeedRepository.saveAndFlush(ReportedFeed.create(feed, user, reportedType));
    }

    @Transactional(readOnly = true)
    public int countByFeedAndReportedType(Feed feed, ReportedType reportedType) {
        return reportedFeedRepository.countByFeedAndReportedType(feed, reportedType);
    }
}
