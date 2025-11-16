package org.websoso.WSSServer.feed.service;

import static java.lang.Boolean.TRUE;
import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;
import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.REPORT;
import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;
import static org.websoso.WSSServer.exception.error.CustomAvatarError.AVATAR_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomCommentError.ALREADY_REPORTED_COMMENT;
import static org.websoso.WSSServer.exception.error.CustomCommentError.COMMENT_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomCommentError.SELF_REPORT_NOT_ALLOWED;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import java.util.AbstractMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.NotificationType;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserDevice;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.dto.comment.CommentGetResponse;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.user.UserBasicInfo;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.ReportedComment;
import org.websoso.WSSServer.feed.repository.CommentRepository;
import org.websoso.WSSServer.feed.repository.ReportedCommentRepository;
import org.websoso.WSSServer.notification.FCMClient;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.repository.NovelRepository;
import org.websoso.WSSServer.repository.AvatarRepository;
import org.websoso.WSSServer.repository.BlockRepository;
import org.websoso.WSSServer.repository.NotificationRepository;
import org.websoso.WSSServer.repository.NotificationTypeRepository;
import org.websoso.WSSServer.repository.UserRepository;
import org.websoso.WSSServer.service.DiscordMessageClient;
import org.websoso.WSSServer.service.MessageFormatter;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final BlockRepository blockRepository;
    private final NovelRepository novelRepository;
    private final UserRepository userRepository;
    private final ReportedCommentRepository reportedCommentRepository;
    private final AvatarRepository avatarRepository;
    private final DiscordMessageClient discordMessageClient;
    private final FCMClient fcmClient;

    private static final int NOTIFICATION_TITLE_MAX_LENGTH = 12;
    private static final int NOTIFICATION_TITLE_MIN_LENGTH = 0;

    public void createComment(User user, Feed feed, String commentContent) {
        commentRepository.save(Comment.create(user.getUserId(), feed, commentContent));
        sendCommentPushMessageToFeedOwner(user, feed);
        sendCommentPushMessageToCommenters(user, feed);
    }

    private void sendCommentPushMessageToFeedOwner(User user, Feed feed) {
        User feedOwner = feed.getUser();
        if (isUserCommentOwner(user, feedOwner) || blockRepository.existsByBlockingIdAndBlockedId(feedOwner.getUserId(),
                user.getUserId())) {
            return;
        }

        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("댓글");

        String notificationTitle = createNotificationTitle(feed);
        String notificationBody = String.format("%s님이 내 글에 댓글을 남겼어요.", user.getNickname());
        Long feedId = feed.getFeedId();

        Notification notification = Notification.create(notificationTitle, notificationBody, null,
                feedOwner.getUserId(), feedId, notificationTypeComment);
        notificationRepository.save(notification);

        if (!TRUE.equals(feedOwner.getIsPushEnabled())) {
            return;
        }

        List<UserDevice> feedOwnerDevices = feedOwner.getUserDevices();
        if (feedOwnerDevices.isEmpty()) {
            return;
        }

        FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(notificationTitle, notificationBody,
                String.valueOf(feedId), "feedDetail", String.valueOf(notification.getNotificationId()));

        List<String> targetFCMTokens = feedOwnerDevices.stream().map(UserDevice::getFcmToken).toList();

        fcmClient.sendMulticastPushMessage(targetFCMTokens, fcmMessageRequest);
    }

    private String createNotificationTitle(Feed feed) {
        if (feed.getNovelId() == null) {
            String feedContent = feed.getFeedContent();
            feedContent = feedContent.length() <= NOTIFICATION_TITLE_MAX_LENGTH ? feedContent
                    : feedContent.substring(NOTIFICATION_TITLE_MIN_LENGTH, NOTIFICATION_TITLE_MAX_LENGTH);
            return "'" + feedContent + "...'";
        }
        Novel novel = novelRepository.findById(feed.getNovelId())
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"));
        return novel.getTitle();
    }

    private void sendCommentPushMessageToCommenters(User user, Feed feed) {
        User feedOwner = feed.getUser();

        List<User> commenters = feed.getComments().stream().map(Comment::getUserId)
                .filter(userId -> !userId.equals(user.getUserId()))
                .filter(userId -> !userId.equals(feedOwner.getUserId()))
                .filter(userId -> !blockRepository.existsByBlockingIdAndBlockedId(userId, user.getUserId())
                        && !blockRepository.existsByBlockingIdAndBlockedId(userId, feed.getUser().getUserId()))
                .distinct().map(userId -> userRepository.findById(userId).orElseThrow(
                        () -> new CustomUserException(USER_NOT_FOUND, "user with the given id was not found")))
                .toList();

        if (commenters.isEmpty()) {
            return;
        }

        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("댓글");

        String notificationTitle = createNotificationTitle(feed);
        String notificationBody = "내가 댓글 단 글에 또 다른 댓글이 달렸어요.";
        Long feedId = feed.getFeedId();

        commenters.forEach(commenter -> {
            Notification notification = Notification.create(notificationTitle, notificationBody, null,
                    commenter.getUserId(), feedId, notificationTypeComment);
            notificationRepository.save(notification);

            if (!TRUE.equals(commenter.getIsPushEnabled())) {
                return;
            }

            List<UserDevice> commenterDevices = commenter.getUserDevices();
            if (commenterDevices.isEmpty()) {
                return;
            }

            List<String> targetFCMTokens = commenterDevices.stream().map(UserDevice::getFcmToken).distinct().toList();

            FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(notificationTitle, notificationBody,
                    String.valueOf(feedId), "feedDetail", String.valueOf(notification.getNotificationId()));
            fcmClient.sendMulticastPushMessage(targetFCMTokens, fcmMessageRequest);
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
        List<CommentGetResponse> responses = feed.getComments().stream()
                .map(comment -> new AbstractMap.SimpleEntry<>(comment, userRepository.findById(comment.getUserId())
                        .orElseThrow(
                                () -> new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"))))
                .map(entry -> CommentGetResponse.of(getUserBasicInfo(entry.getValue()), entry.getKey(),
                        isUserCommentOwner(entry.getValue(), user), entry.getKey().getIsSpoiler(),
                        isBlocked(user, entry.getValue()), entry.getKey().getIsHidden())).toList();

        return CommentsGetResponse.of(responses);
    }

    public void createReportedComment(Feed feed, Long commentId, User user, ReportedType reportedType) {
        Comment comment = getCommentOrException(commentId);

        comment.validateFeedAssociation(feed);

        User commentCreatedUser = userRepository.findById(comment.getUserId())
                .orElseThrow(() -> new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        if (isUserCommentOwner(commentCreatedUser, user)) {
            throw new CustomCommentException(SELF_REPORT_NOT_ALLOWED, "cannot report own comment");
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

    private Comment getCommentOrException(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new CustomCommentException(COMMENT_NOT_FOUND, "comment with the given id was not found"));
    }

    private UserBasicInfo getUserBasicInfo(User user) {
        return user.getUserBasicInfo(
                avatarRepository.findById(user.getAvatarId()).orElseThrow(() ->
                                new CustomAvatarException(AVATAR_NOT_FOUND, "avatar with the given id was not found"))
                        .getAvatarImage()
        );
    }

    private Boolean isUserCommentOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private Boolean isBlocked(User user, User createdFeedUser) {
        return blockRepository.existsByBlockingIdAndBlockedId(user.getUserId(), createdFeedUser.getUserId());
    }
}
