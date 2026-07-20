package org.websoso.WSSServer.feed.report.application;

import static org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessageType.REPORT;
import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;
import static org.websoso.WSSServer.feed.comment.exception.CustomCommentError.ALREADY_REPORTED_COMMENT;
import static org.websoso.WSSServer.feed.feed.exception.CustomFeedError.ALREADY_REPORTED_FEED;
import static org.websoso.WSSServer.feed.feed.exception.CustomFeedError.SELF_REPORT_NOT_ALLOWED;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;
import org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessage;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.comment.exception.CustomCommentError;
import org.websoso.WSSServer.feed.comment.exception.CustomCommentException;
import org.websoso.WSSServer.feed.feed.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.comment.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.feed.report.service.ReportServiceImpl;
import org.websoso.WSSServer.notification.service.MessageFormatter;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.UserRepository;
import org.websoso.WSSServer.user.service.BlockService;

@Service
@RequiredArgsConstructor
public class ReportApplication {

    private final FeedServiceImpl feedServiceImpl;
    private final CommentServiceImpl commentServiceImpl;
    private final ReportServiceImpl reportServiceImpl;
    private final DiscordMessageClient discordMessageClient;
    private final BlockService blockService;

    //ToDo : 의존성 제거 필요 부분
    private final UserRepository userRepository;

    @Transactional
    public void reportComment(User user, Long feedId, Long commentId, ReportedType reportedType) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);
        Comment comment = commentServiceImpl.getCommentOrException(commentId);
        comment.validateBelongsTo(feed);

        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());
        if (!comment.getUserId().equals(feed.getWriterId())) {
            blockService.validateNotBlocked(user.getUserId(), comment.getUserId());
        }

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
                comment.markSpoiler();
            } else if (reportedType.equals(IMPERTINENCE)) {
                comment.markHidden();
            }
        }

        discordMessageClient.sendDiscordWebhookMessage(DiscordWebhookMessage.of(
                MessageFormatter.formatCommentReportMessage(user, feed, comment, reportedType, commentCreatedUser,
                        reportedCount, shouldHide), REPORT));
    }

    @Transactional
    public void reportFeed(User user, Long feedId, ReportedType reportedType) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);

        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());

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
