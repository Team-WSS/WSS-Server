package org.websoso.WSSServer.feed.service;

import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.REPORT;
import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;
import static org.websoso.WSSServer.exception.error.CustomCommentError.ALREADY_REPORTED_COMMENT;
import static org.websoso.WSSServer.exception.error.CustomCommentError.COMMENT_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomFeedError.ALREADY_REPORTED_FEED;
import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomFeedError.SELF_REPORT_NOT_ALLOWED;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.exception.error.CustomCommentError;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.ReportedComment;
import org.websoso.WSSServer.feed.domain.ReportedFeed;
import org.websoso.WSSServer.feed.repository.CommentRepository;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.feed.repository.ReportedCommentRepository;
import org.websoso.WSSServer.feed.repository.ReportedFeedRepository;
import org.websoso.WSSServer.user.repository.UserRepository;
import org.websoso.WSSServer.notification.service.DiscordMessageClient;
import org.websoso.WSSServer.service.MessageFormatter;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportedCommentRepository reportedCommentRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final ReportedFeedRepository reportedFeedRepository;
    private final DiscordMessageClient discordMessageClient;

    @Transactional
    public void reportComment(User user, Long feedId, Long commentId, ReportedType reportedType) {
        Feed feed = getFeedOrException(feedId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new CustomCommentException(COMMENT_NOT_FOUND, "comment with the given id was not found"));
        comment.validateFeedAssociation(feed);

        User commentCreatedUser = userRepository.findById(comment.getUserId())
                .orElseThrow(() -> new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        if (commentCreatedUser.equals(user)) {
            throw new CustomCommentException(CustomCommentError.SELF_REPORT_NOT_ALLOWED, "cannot report own comment");
        }

        if (reportedCommentRepository.existsByCommentAndUserAndReportedType(comment, user, reportedType)) {
            throw new CustomCommentException(ALREADY_REPORTED_COMMENT, "comment has already been reported by the user");
        }

        reportedCommentRepository.save(ReportedComment.create(comment, user, reportedType));

        int reportedCount = reportedCommentRepository.countByCommentAndReportedType(comment, reportedType);
        boolean shouldHide = reportedType.isExceedingLimit(reportedCount);

        if (shouldHide) {
            if (reportedType.equals(SPOILER)) {
                comment.spoiler();
            } else if (reportedType.equals(IMPERTINENCE)) {
                comment.hideComment();
            }
        }

        discordMessageClient.sendDiscordWebhookMessage(DiscordWebhookMessage.of(
                MessageFormatter.formatCommentReportMessage(user, feed, comment, reportedType, commentCreatedUser,
                        reportedCount, shouldHide), REPORT));
    }

    private Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }

    @Transactional
    public void reportFeed(User user, Long feedId, ReportedType reportedType) {
        Feed feed = getFeedOrException(feedId);

        if (isUserFeedOwner(feed.getUser(), user)) {
            throw new CustomFeedException(SELF_REPORT_NOT_ALLOWED, "cannot report own feed");
        }

        if (reportedFeedRepository.existsByFeedAndUserAndReportedType(feed, user, reportedType)) {
            throw new CustomFeedException(ALREADY_REPORTED_FEED, "feed has already been reported by the user");
        }

        reportedFeedRepository.save(ReportedFeed.create(feed, user, reportedType));

        int reportedCount = reportedFeedRepository.countByFeedAndReportedType(feed, reportedType);
        boolean shouldHide = reportedType.isExceedingLimit(reportedCount);

        if (shouldHide) {
            feed.hideFeed();
        }

        discordMessageClient.sendDiscordWebhookMessage(DiscordWebhookMessage.of(
                MessageFormatter.formatFeedReportMessage(user, feed, reportedType, reportedCount, shouldHide), REPORT));
    }

    private Boolean isUserFeedOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }
}
