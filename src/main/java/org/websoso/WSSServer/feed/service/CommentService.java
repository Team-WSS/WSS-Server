package org.websoso.WSSServer.feed.service;

import static java.lang.Boolean.TRUE;
import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;
import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.REPORT;
import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;
import static org.websoso.WSSServer.exception.error.CustomCommentError.COMMENT_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomCommentError.SELF_REPORT_NOT_ALLOWED;

import java.util.AbstractMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.NotificationType;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserDevice;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.dto.comment.CommentGetResponse;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.user.UserBasicInfo;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.notification.FCMService;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;
import org.websoso.WSSServer.feed.repository.CommentRepository;
import org.websoso.WSSServer.repository.NotificationRepository;
import org.websoso.WSSServer.repository.NotificationTypeRepository;
import org.websoso.WSSServer.service.AvatarService;
import org.websoso.WSSServer.service.BlockService;
import org.websoso.WSSServer.service.MessageFormatter;
import org.websoso.WSSServer.service.MessageService;
import org.websoso.WSSServer.novel.service.NovelService;
import org.websoso.WSSServer.service.UserService;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final AvatarService avatarService;
    private final BlockService blockService;
    private final ReportedCommentService reportedCommentService;
    private final MessageService messageService;
    private final FCMService fcmService;
    private final NovelService novelService;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationRepository notificationRepository;

    public void createComment(User user, Feed feed, String commentContent) {
        commentRepository.save(Comment.create(user.getUserId(), feed, commentContent));
        sendCommentPushMessageToFeedOwner(user, feed);
        sendCommentPushMessageToCommenters(user, feed);
    }

    private void sendCommentPushMessageToFeedOwner(User user, Feed feed) {
        User feedOwner = feed.getUser();
        if (isUserCommentOwner(user, feedOwner) || blockService.isBlocked(feedOwner.getUserId(), user.getUserId())) {
            return;
        }

        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("댓글");

        String notificationTitle = createNotificationTitle(feed);
        String notificationBody = String.format("%s님이 내 글에 댓글을 남겼어요.", user.getNickname());
        Long feedId = feed.getFeedId();

        Notification notification = Notification.create(
                notificationTitle,
                notificationBody,
                null,
                feedOwner.getUserId(),
                feedId,
                notificationTypeComment
        );
        notificationRepository.save(notification);

        if (!TRUE.equals(feedOwner.getIsPushEnabled())) {
            return;
        }

        List<UserDevice> feedOwnerDevices = feedOwner.getUserDevices();
        if (feedOwnerDevices.isEmpty()) {
            return;
        }

        FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(
                notificationTitle,
                notificationBody,
                String.valueOf(feedId),
                "feedDetail",
                String.valueOf(notification.getNotificationId())
        );

        List<String> targetFCMTokens = feedOwnerDevices
                .stream()
                .map(UserDevice::getFcmToken)
                .toList();

        fcmService.sendMulticastPushMessage(
                targetFCMTokens,
                fcmMessageRequest
        );
    }

    private String createNotificationTitle(Feed feed) {
        if (feed.getNovelId() == null) {
            String feedContent = feed.getFeedContent();
            feedContent = feedContent.length() <= 12
                    ? feedContent
                    : feedContent.substring(0, 12);
            return "'" + feedContent + "...'";
        }
        Novel novel = novelService.getNovelOrException(feed.getNovelId());
        return novel.getTitle();
    }

    private void sendCommentPushMessageToCommenters(User user, Feed feed) {
        User feedOwner = feed.getUser();

        List<User> commenters = feed.getComments()
                .stream()
                .map(Comment::getUserId)
                .filter(userId -> !userId.equals(user.getUserId()))
                .filter(userId -> !userId.equals(feedOwner.getUserId()))
                .filter(userId -> !blockService.isBlocked(userId, user.getUserId())
                        && !blockService.isBlocked(userId, feed.getUser().getUserId()))
                .distinct()
                .map(userService::getUserOrException)
                .toList();

        if (commenters.isEmpty()) {
            return;
        }

        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("댓글");

        String notificationTitle = createNotificationTitle(feed);
        String notificationBody = "내가 댓글 단 글에 또 다른 댓글이 달렸어요.";
        Long feedId = feed.getFeedId();

        commenters.forEach(commenter -> {
            Notification notification = Notification.create(
                    notificationTitle,
                    notificationBody,
                    null,
                    commenter.getUserId(),
                    feedId,
                    notificationTypeComment
            );
            notificationRepository.save(notification);

            if (!TRUE.equals(commenter.getIsPushEnabled())) {
                return;
            }

            List<UserDevice> commenterDevices = commenter.getUserDevices();
            if (commenterDevices.isEmpty()) {
                return;
            }

            List<String> targetFCMTokens = commenterDevices
                    .stream()
                    .map(UserDevice::getFcmToken)
                    .distinct()
                    .toList();

            FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(
                    notificationTitle,
                    notificationBody,
                    String.valueOf(feedId),
                    "feedDetail",
                    String.valueOf(notification.getNotificationId())
            );
            fcmService.sendMulticastPushMessage(
                    targetFCMTokens,
                    fcmMessageRequest
            );
        });
    }

    public void updateComment(Long userId, Feed feed, Long commentId, String commentContent) {
        Comment comment = getCommentOrException(commentId);
        comment.validateFeedAssociation(feed);
        comment.validateUserAuthorization(userId, UPDATE);
        comment.updateContent(commentContent);
    }

    public void deleteComment(Long userId, Feed feed, Long commentId) {
        Comment comment = getCommentOrException(commentId);
        comment.validateFeedAssociation(feed);
        comment.validateUserAuthorization(userId, DELETE);
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public CommentsGetResponse getComments(User user, Feed feed) {
        List<CommentGetResponse> responses = feed.getComments()
                .stream()
                .map(comment -> new AbstractMap.SimpleEntry<>(
                        comment, userService.getUserOrException(comment.getUserId())))
                .map(entry -> CommentGetResponse.of(
                        getUserBasicInfo(entry.getValue()),
                        entry.getKey(),
                        isUserCommentOwner(entry.getValue(), user),
                        entry.getKey().getIsSpoiler(),
                        isBlocked(user, entry.getValue()),
                        entry.getKey().getIsHidden()))
                .toList();

        return CommentsGetResponse.of(responses);
    }

    public void createReportedComment(Feed feed, Long commentId, User user, ReportedType reportedType) {
        Comment comment = getCommentOrException(commentId);

        comment.validateFeedAssociation(feed);

        User commentCreatedUser = userService.getUserOrException(comment.getUserId());

        if (isUserCommentOwner(commentCreatedUser, user)) {
            throw new CustomCommentException(SELF_REPORT_NOT_ALLOWED, "cannot report own comment");
        }

        reportedCommentService.createReportedComment(comment, user, reportedType);

        int reportedCount = reportedCommentService.getReportedCountByReportedType(comment, reportedType);
        boolean shouldHide = reportedType.isExceedingLimit(reportedCount);

        if (shouldHide) {
            if (reportedType.equals(SPOILER)) {
                comment.spoiler();
            } else if (reportedType.equals(IMPERTINENCE)) {
                comment.hideComment();
            }
        }

        messageService.sendDiscordWebhookMessage(DiscordWebhookMessage.of(
                MessageFormatter.formatCommentReportMessage(user, feed, comment, reportedType,
                        commentCreatedUser, reportedCount,
                        shouldHide), REPORT));
    }

    private Comment getCommentOrException(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new CustomCommentException(COMMENT_NOT_FOUND, "comment with the given id was not found"));
    }

    private UserBasicInfo getUserBasicInfo(User user) {
        return user.getUserBasicInfo(
                avatarService.getAvatarOrException(user.getAvatarId()).getAvatarImage()
        );
    }

    private Boolean isUserCommentOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private Boolean isBlocked(User user, User createdFeedUser) {
        return blockService.isBlocked(user.getUserId(), createdFeedUser.getUserId());
    }

}
