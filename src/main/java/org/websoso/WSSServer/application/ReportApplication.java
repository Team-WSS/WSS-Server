package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.REPORT;
import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;
import static org.websoso.WSSServer.exception.error.CustomCommentError.ALREADY_REPORTED_COMMENT;
import static org.websoso.WSSServer.exception.error.CustomFeedError.ALREADY_REPORTED_FEED;
import static org.websoso.WSSServer.exception.error.CustomFeedError.SELF_REPORT_NOT_ALLOWED;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.exception.error.CustomCommentError;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.feed.service.ReportServiceImpl;
import org.websoso.WSSServer.service.DiscordMessageClient;
import org.websoso.WSSServer.service.MessageFormatter;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ReportApplication {

    private final FeedServiceImpl feedServiceImpl;
    private final CommentServiceImpl commentServiceImpl;
    private final ReportServiceImpl reportServiceImpl;
    private final DiscordMessageClient discordMessageClient;

    //ToDo : 의존성 제거 필요 부분
    private final UserRepository userRepository;

    @Transactional
    public void reportComment(User user, Long feedId, Long commentId, ReportedType reportedType) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);
        Comment comment = commentServiceImpl.findComment(commentId);
        comment.validateFeedAssociation(feed);

        User commentCreatedUser = userRepository.findById(comment.getUserId())
                .orElseThrow(() -> new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        if (commentCreatedUser.equals(user)) {
            throw new CustomCommentException(CustomCommentError.SELF_REPORT_NOT_ALLOWED, "cannot report own comment");
        }

        if (reportServiceImpl.isExistsByCommentAndUserAndReportedType(comment, user, reportedType)) {
            throw new CustomCommentException(ALREADY_REPORTED_COMMENT, "comment has already been reported by the user");
        }

        reportServiceImpl.saveReportedComment(comment, user, reportedType);

        int reportedCount = reportServiceImpl.countByCommentAndReportedType(comment, reportedType);
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

    @Transactional
    public void reportFeed(User user, Long feedId, ReportedType reportedType) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);

        if (isUserFeedOwner(feed.getUser(), user)) {
            throw new CustomFeedException(SELF_REPORT_NOT_ALLOWED, "cannot report own feed");
        }

        if (reportServiceImpl.isExistsByFeedAndUserAndReportedType(feed, user, reportedType)) {
            throw new CustomFeedException(ALREADY_REPORTED_FEED, "feed has already been reported by the user");
        }

        reportServiceImpl.saveReportedFeed(feed, user, reportedType);

        int reportedCount = reportServiceImpl.countByFeedAndReportedType(feed, reportedType);
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
