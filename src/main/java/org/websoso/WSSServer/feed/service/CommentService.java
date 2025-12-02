package org.websoso.WSSServer.feed.service;

import static java.lang.Boolean.TRUE;
import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;
import static org.websoso.WSSServer.exception.error.CustomAvatarError.AVATAR_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomCommentError.COMMENT_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import java.util.AbstractMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.NotificationType;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.domain.UserDevice;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentGetResponse;
import org.websoso.WSSServer.dto.comment.CommentUpdateRequest;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.user.UserBasicInfo;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.repository.CommentRepository;
import org.websoso.WSSServer.feed.repository.FeedRepository;
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

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final FeedRepository feedRepository;
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

    @Transactional
    public void createComment(User user, Long feedId, CommentCreateRequest request) {
        Feed feed = getFeedOrException(feedId);
        commentRepository.save(Comment.create(user.getUserId(), feed, request.commentContent()));
        sendCommentPushMessageToFeedOwner(user, feed);
        sendCommentPushMessageToCommenters(user, feed);
    }

    private Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }

    private void sendCommentPushMessageToFeedOwner(User user, Feed feed) {
        User feedOwner = feed.getUser();
        if (user.equals(feedOwner) || blockRepository.existsByBlockingIdAndBlockedId(feedOwner.getUserId(),
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

    //ToDo : CommentService와 중복되는 부분 추출 필요
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

    @Transactional(readOnly = true)
    public CommentsGetResponse getComments(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);
        List<CommentGetResponse> responses = feed.getComments().stream()
                .map(comment -> new AbstractMap.SimpleEntry<>(comment, userRepository.findById(comment.getUserId())
                        .orElseThrow(
                                () -> new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"))))
                .map(entry -> CommentGetResponse.of(getUserBasicInfo(entry.getValue()), entry.getKey(),
                        isUserCommentOwner(entry.getValue(), user), entry.getKey().getIsSpoiler(),
                        isBlocked(user, entry.getValue()), entry.getKey().getIsHidden())).toList();

        return CommentsGetResponse.of(responses);
    }

    private Boolean isUserCommentOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private Boolean isBlocked(User user, User createdFeedUser) {
        return blockRepository.existsByBlockingIdAndBlockedId(user.getUserId(), createdFeedUser.getUserId());
    }

    private UserBasicInfo getUserBasicInfo(User user) {
        return user.getUserBasicInfo(
                avatarRepository.findById(user.getAvatarId()).orElseThrow(() ->
                                new CustomAvatarException(AVATAR_NOT_FOUND, "avatar with the given id was not found"))
                        .getAvatarImage());
    }

    @Transactional
    public void updateComment(User user, Long feedId, Long commentId, CommentUpdateRequest request) {
        Feed feed = getFeedOrException(feedId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new CustomCommentException(COMMENT_NOT_FOUND, "comment with the given id was not found"));
        comment.validateFeedAssociation(feed);
        comment.validateUserAuthorization(user.getUserId(), UPDATE);
        comment.updateContent(request.commentContent());
    }

    @Transactional
    public void deleteComment(User user, Long feedId, Long commentId) {
        Feed feed = getFeedOrException(feedId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new CustomCommentException(COMMENT_NOT_FOUND, "comment with the given id was not found"));
        comment.validateFeedAssociation(feed);
        comment.validateUserAuthorization(user.getUserId(), DELETE);
        commentRepository.delete(comment);
    }
}
