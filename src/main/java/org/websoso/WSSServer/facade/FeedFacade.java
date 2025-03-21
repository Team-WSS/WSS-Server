package org.websoso.WSSServer.facade;

import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.REPORT;
import static org.websoso.WSSServer.exception.error.CustomFeedError.BLOCKED_USER_ACCESS;
import static org.websoso.WSSServer.exception.error.CustomFeedError.HIDDEN_FEED_ACCESS;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentUpdateRequest;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedGetResponse;
import org.websoso.WSSServer.dto.feed.FeedUpdateRequest;
import org.websoso.WSSServer.dto.feed.FeedsGetResponse;
import org.websoso.WSSServer.dto.feed.InterestFeedsGetResponse;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedsGetResponse;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.service.BlockService;
import org.websoso.WSSServer.service.CommentService;
import org.websoso.WSSServer.service.FeedService;
import org.websoso.WSSServer.service.LikeService;
import org.websoso.WSSServer.service.MessageFormatter;
import org.websoso.WSSServer.service.MessageService;
import org.websoso.WSSServer.service.NotificationService;
import org.websoso.WSSServer.service.PopularFeedService;
import org.websoso.WSSServer.service.ReportService;
import org.websoso.WSSServer.service.UserService;

@Component
@RequiredArgsConstructor
public class FeedFacade {
    private static final int POPULAR_FEED_LIKE_THRESHOLD = 5;
    private final FeedService feedService;
    private final PopularFeedService popularFeedService;
    private final LikeService likeService;
    private final CommentService commentService;
    private final ReportService reportService;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final BlockService blockService;

    public void createFeed(User user, FeedCreateRequest request) {
        feedService.createFeed(user, request);
    }

    @Transactional(readOnly = true)
    public FeedGetResponse getFeed(User user, Long feedId) {
        return feedService.getFeedById(user, feedId);
    }

    @Transactional(readOnly = true)
    public FeedsGetResponse getFeeds(User user, String category, Long lastFeedId, int size) {
        return feedService.getFeeds(user, category, lastFeedId, size);
    }

    public void updateFeed(User user, Long feedId, FeedUpdateRequest request) {
        feedService.updateFeed(user, feedId, request);
    }

    public void deleteFeed(User user, Long feedId) {
        feedService.deleteFeed(user, feedId);
    }

    public void reportFeed(User user, Long feedId, ReportedType reportedType) {
        Feed feed = getFeedOrException(feedId);

        validate(feed, user.getUserId(), feed.getUser().getUserId());

        int reportedCount = reportService.reportFeed(user, feed, reportedType);
        messageService.sendDiscordWebhookMessage(DiscordWebhookMessage.of(
                MessageFormatter.formatFeedReportMessage(user, feed, reportedType, reportedCount), REPORT));
    }

    public void reportComment(User reporteCreatedUser, Long feedId, Long commentId, ReportedType reportedType) {
        Feed feed = getFeedOrException(feedId);
        Comment comment = commentService.getCommentOrException(commentId);

        validate(feed, reporteCreatedUser.getUserId(), comment.getUserId());

        User commentCreatedUser = userService.getUserOrException(comment.getUserId());
        int reportedCount = reportService.reportComment(reporteCreatedUser, getFeedOrException(feedId), comment,
                commentCreatedUser, reportedType);
        messageService.sendDiscordWebhookMessage(DiscordWebhookMessage.of(
                MessageFormatter.formatCommentReportMessage(reporteCreatedUser, feed, comment, reportedType,
                        commentCreatedUser, reportedCount), REPORT));
    }

    @Transactional(readOnly = true)
    public PopularFeedsGetResponse getPopularFeeds(User user) {
        return popularFeedService.getPopularFeeds(user);
    }

    @Transactional(readOnly = true)
    public InterestFeedsGetResponse getInterestFeeds(User user) {
        return feedService.getInterestFeeds(user);
    }

    public void likeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);
        validate(feed, feed.getUser().getUserId(), user.getUserId());

        likeService.createLike(user, feed);

        if (feed.getLikes().size() == POPULAR_FEED_LIKE_THRESHOLD) {
            popularFeedService.createPopularFeed(feed);
            notificationService.sendPopularFeedPushMessage(feed);
        }

        if (blockService.isBlocked(user.getUserId(), feed.getFeedId())) {
            return;
        }
        notificationService.sendLikePushMessage(user, feed);
    }

    public void unlikeFeed(User user, Long feedId) {
        likeService.deleteLike(user, getFeedOrException(feedId));
    }

    public void createComment(User user, Long feedId, CommentCreateRequest request) {
        Feed feed = getFeedOrException(feedId);
        validate(feed, user.getUserId(), feed.getUser().getUserId());
        commentService.createComment(user, getFeedOrException(feedId), request.commentContent());
        sendCommentPushMessage(user, feed);
    }

    @Transactional(readOnly = true)
    public CommentsGetResponse getComments(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);
        validate(feed, user.getUserId(), feed.getUser().getUserId());
        return commentService.getComments(user, getFeedOrException(feedId));
    }

    public void updateComment(User user, Long feedId, Long commentId, CommentUpdateRequest request) {
        Feed feed = getFeedOrException(feedId);
        validate(feed, user.getUserId(), feed.getUser().getUserId());
        commentService.updateComment(user.getUserId(), getFeedOrException(feedId), commentId, request.commentContent());
    }

    public void deleteComment(User user, Long feedId, Long commentId) {
        Feed feed = getFeedOrException(feedId);
        validate(feed, user.getUserId(), feed.getUser().getUserId());
        commentService.deleteComment(user.getUserId(), getFeedOrException(feedId), commentId);
    }

    private Feed getFeedOrException(Long feedId) {
        return feedService.getFeedOrException(feedId);
    }

    private void validate(Feed feed, Long blockingId, Long blockedId) {
        checkHiddenFeed(feed);
        checkBlocked(blockingId, blockedId);
    }

    private void checkHiddenFeed(Feed feed) {
        if (feed.getIsHidden()) {
            throw new CustomFeedException(HIDDEN_FEED_ACCESS, "Cannot access hidden feed.");
        }
    }

    private void checkBlocked(Long blockingId, Long blockedId) {
        if (blockService.isBlocked(blockingId, blockedId)) {
            throw new CustomFeedException(BLOCKED_USER_ACCESS,
                    "cannot access this feed because either you or the feed author has blocked the other.");
        }
    }

    private void sendCommentPushMessage(User user, Feed feed) {
        User feedOwner = feed.getUser();
        notificationService.sendCommentPushMessageToCommenters(user, feed);
        if (isUserCommentOwner(user, feedOwner) || blockService.isBlocked(feedOwner.getUserId(), user.getUserId())) {
            return;
        }
        notificationService.sendCommentPushMessageToFeedOwner(user, feed);
    }

    private boolean isUserCommentOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }
}
