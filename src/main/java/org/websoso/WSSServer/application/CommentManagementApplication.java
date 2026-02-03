package org.websoso.WSSServer.application;

import static java.lang.Boolean.TRUE;
import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.NotificationType;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.notification.domain.UserDevice;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentUpdateRequest;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.notification.FCMClient;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.user.repository.BlockRepository;
import org.websoso.WSSServer.notification.repository.NotificationRepository;
import org.websoso.WSSServer.notification.repository.NotificationTypeRepository;
import org.websoso.WSSServer.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CommentManagementApplication {

    private final CommentServiceImpl commentServiceImpl;
    private final FeedServiceImpl feedServiceImpl;
    private final NovelServiceImpl novelServiceImpl;
    private final FCMClient fcmClient;

    private static final int NOTIFICATION_TITLE_MAX_LENGTH = 12;
    private static final int NOTIFICATION_TITLE_MIN_LENGTH = 0;

    //ToDo : 의존성 제거 필요 부분
    private final BlockRepository blockRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createComment(User user, Long feedId, CommentCreateRequest request) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);
        commentServiceImpl.createComment(user, feed, request);
        sendCommentPushMessageToFeedOwner(user, feed);
        sendCommentPushMessageToCommenters(user, feed);
    }

    private void sendCommentPushMessageToFeedOwner(User user, Feed feed) {
        User feedOwner = feed.getUser();
        if (user.equals(feedOwner) || blockRepository.existsByBlockingIdAndBlockedId(feedOwner.getUserId(),
                user.getUserId())) {
            return;
        }

        // ToDo : 해당 로직 NotificationSerivce에서 처리하도록 수정
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

        // ToDo : 해당 로직 NotificationSerivce에서 처리하도록 수정
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
        Novel novel = novelServiceImpl.getNovelOrException(feed.getNovelId());
        return novel.getTitle();
    }

    @Transactional
    public void updateComment(User user, Long feedId, Long commentId, CommentUpdateRequest request) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);
        Comment comment = commentServiceImpl.findComment(commentId);
        comment.validateFeedAssociation(feed);
        comment.validateUserAuthorization(user.getUserId(), UPDATE);
        commentServiceImpl.updateComment(comment, request);
    }

    @Transactional
    public void deleteComment(User user, Long feedId, Long commentId) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);
        Comment comment = commentServiceImpl.findComment(commentId);
        comment.validateFeedAssociation(feed);
        comment.validateUserAuthorization(user.getUserId(), DELETE);
        commentServiceImpl.deleteComment(comment);
    }
}
