package org.websoso.WSSServer.service;

import static java.lang.Boolean.TRUE;
import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.REPORT;
import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomFeedError.SELF_REPORT_NOT_ALLOWED;
import static org.websoso.WSSServer.exception.error.CustomUserError.PRIVATE_PROFILE_STATUS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.NotificationType;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserDevice;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentUpdateRequest;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedGetResponse;
import org.websoso.WSSServer.dto.feed.FeedInfo;
import org.websoso.WSSServer.dto.feed.FeedUpdateRequest;
import org.websoso.WSSServer.dto.feed.FeedsGetResponse;
import org.websoso.WSSServer.dto.feed.InterestFeedGetResponse;
import org.websoso.WSSServer.dto.feed.InterestFeedsGetResponse;
import org.websoso.WSSServer.dto.feed.UserFeedGetResponse;
import org.websoso.WSSServer.dto.feed.UserFeedsGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponseFeedTab;
import org.websoso.WSSServer.dto.user.UserBasicInfo;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.notification.FCMService;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;
import org.websoso.WSSServer.repository.AvatarRepository;
import org.websoso.WSSServer.repository.FeedRepository;
import org.websoso.WSSServer.repository.NotificationRepository;
import org.websoso.WSSServer.repository.NotificationTypeRepository;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedService {

    private static final String DEFAULT_CATEGORY = "all";
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int POPULAR_FEED_LIKE_THRESHOLD = 5;
    private final FeedRepository feedRepository;
    private final FeedCategoryService feedCategoryService;
    private final NovelService novelService;
    private final AvatarService avatarService;
    private final BlockService blockService;
    private final LikeService likeService;
    private final PopularFeedService popularFeedService;
    private final UserNovelRepository userNovelRepository;
    private final AvatarRepository avatarRepository;
    private final CommentService commentService;
    private final ReportedFeedService reportedFeedService;
    private final MessageService messageService;
    private final UserService userService;
    private final NovelRepository novelRepository;
    private final FCMService fcmService;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationRepository notificationRepository;

    public void createFeed(User user, FeedCreateRequest request) {
        Optional.ofNullable(request.novelId())
                .ifPresent(novelService::getNovelOrException);
        Feed feed = Feed.create(request.feedContent(), request.novelId(), request.isSpoiler(), user);
        feedRepository.save(feed);
        feedCategoryService.createFeedCategory(feed, request.relevantCategories());
    }

    public void updateFeed(Long feedId, FeedUpdateRequest request) {
        Feed feed = getFeedOrException(feedId);

        if (request.novelId() != null && feed.isNovelChanged(request.novelId())) {
            novelService.getNovelOrException(request.novelId());
        }
        feed.updateFeed(request.feedContent(), request.isSpoiler(), request.novelId());
        feedCategoryService.updateFeedCategory(feed, request.relevantCategories());
    }

    public void deleteFeed(Long feedId) {
        feedRepository.deleteById(feedId);
    }

    public void likeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        likeService.createLike(user, feed);
        if (feed.getLikes().size() == POPULAR_FEED_LIKE_THRESHOLD) {
            popularFeedService.createPopularFeed(feed);
        }

        sendLikePushMessage(user, feed);
    }

    private void sendLikePushMessage(User liker, Feed feed) {
        User feedOwner = feed.getUser();
        if (liker.equals(feedOwner) || blockService.isBlocked(feedOwner.getUserId(), liker.getUserId())) {
            return;
        }

        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("좋아요");

        String notificationTitle = createNotificationTitle(feed);
        String notificationBody = String.format("%s님이 내 수다글을 좋아해요.", liker.getNickname());
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

    public void unLikeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);
        likeService.deleteLike(user, feed);
    }

    @Transactional(readOnly = true)
    public FeedGetResponse getFeedById(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);
        UserBasicInfo userBasicInfo = getUserBasicInfo(feed.getUser());
        Novel novel = getLinkedNovelOrNull(feed.getNovelId());
        Boolean isLiked = isUserLikedFeed(user, feed);
        List<String> relevantCategories = feedCategoryService.getRelevantCategoryNames(feed.getFeedCategories());
        Boolean isMyFeed = isUserFeedOwner(feed.getUser(), user);

        return FeedGetResponse.of(feed, userBasicInfo, novel, isLiked, relevantCategories, isMyFeed);
    }

    @Transactional(readOnly = true)
    public FeedsGetResponse getFeeds(User user, String category, Long lastFeedId, int size) {
        Slice<Feed> feeds = findFeedsByCategoryLabel(category == null ? DEFAULT_CATEGORY : category,
                lastFeedId, user == null ? null : user.getUserId(), PageRequest.of(DEFAULT_PAGE_NUMBER, size));

        List<FeedInfo> feedGetResponses = feeds.getContent().stream()
                .map(feed -> createFeedInfo(feed, user)).toList();

        return FeedsGetResponse.of(category == null ? DEFAULT_CATEGORY : category, feeds.hasNext(),
                feedGetResponses);
    }

    public void createComment(User user, Long feedId, CommentCreateRequest request) {
        Feed feed = getFeedOrException(feedId);
        commentService.createComment(user, feed, request.commentContent());
    }

    public void updateComment(User user, Long feedId, Long commentId, CommentUpdateRequest request) {
        Feed feed = getFeedOrException(feedId);
        commentService.updateComment(user.getUserId(), feed, commentId, request.commentContent());
    }

    public void deleteComment(User user, Long feedId, Long commentId) {
        Feed feed = getFeedOrException(feedId);
        commentService.deleteComment(user.getUserId(), feed, commentId);
    }

    @Transactional(readOnly = true)
    public CommentsGetResponse getComments(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);
        return commentService.getComments(user, feed);
    }

    public void reportFeed(User user, Long feedId, ReportedType reportedType) {
        Feed feed = getFeedOrException(feedId);

        if (isUserFeedOwner(feed.getUser(), user)) {
            throw new CustomFeedException(SELF_REPORT_NOT_ALLOWED, "cannot report own feed");
        }

        reportedFeedService.createReportedFeed(feed, user, reportedType);

        int reportedCount = reportedFeedService.getReportedCountByReportedType(feed, reportedType);
        boolean shouldHide = reportedType.isExceedingLimit(reportedCount);

        if (shouldHide) {
            feed.hideFeed();
        }

        messageService.sendDiscordWebhookMessage(DiscordWebhookMessage.of(
                MessageFormatter.formatFeedReportMessage(user, feed, reportedType, reportedCount, shouldHide), REPORT));
    }

    public void reportComment(User user, Long feedId, Long commentId, ReportedType reportedType) {
        Feed feed = getFeedOrException(feedId);
        commentService.createReportedComment(feed, commentId, user, reportedType);
    }

    private Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() ->
                new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }

    private UserBasicInfo getUserBasicInfo(User user) {
        return user.getUserBasicInfo(
                avatarService.getAvatarOrException(user.getAvatarId()).getAvatarImage()
        );
    }

    private Novel getLinkedNovelOrNull(Long linkedNovelId) {
        if (linkedNovelId == null) {
            return null;
        }
        return novelService.getNovelOrException(linkedNovelId);
    }

    private Boolean isUserLikedFeed(User user, Feed feed) {
        return likeService.isUserLikedFeed(user, feed);
    }

    private Boolean isUserFeedOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private FeedInfo createFeedInfo(Feed feed, User user) {
        UserBasicInfo userBasicInfo = getUserBasicInfo(feed.getUser());
        Novel novel = getLinkedNovelOrNull(feed.getNovelId());
        Boolean isLiked = user != null && isUserLikedFeed(user, feed);
        List<String> relevantCategories = feedCategoryService.getRelevantCategoryNames(feed.getFeedCategories());
        Boolean isMyFeed = user != null && isUserFeedOwner(feed.getUser(), user);

        return FeedInfo.of(feed, userBasicInfo, novel, isLiked, relevantCategories, isMyFeed);
    }

    private Slice<Feed> findFeedsByCategoryLabel(String category, Long lastFeedId, Long userId,
                                                 PageRequest pageRequest) {
        if (category.equals(DEFAULT_CATEGORY)) {
            return (feedRepository.findFeeds(lastFeedId, userId, pageRequest));
        }
        return feedCategoryService.getFeedsByCategoryLabel(category, lastFeedId, userId, pageRequest);
    }

    public InterestFeedsGetResponse getInterestFeeds(User user) {
        List<Novel> interestNovels = userNovelRepository.findByUserAndIsInterestTrue(user)
                .stream()
                .map(UserNovel::getNovel)
                .toList();

        if (interestNovels.isEmpty()) {
            return InterestFeedsGetResponse.of(Collections.emptyList(), "NO_INTEREST_NOVELS");
        }

        Map<Long, Novel> novelMap = interestNovels
                .stream()
                .collect(Collectors.toMap(Novel::getNovelId, novel -> novel));
        List<Long> interestNovelIds = new ArrayList<>(novelMap.keySet());

        List<Feed> interestFeeds = feedRepository.findTop10ByNovelIdInOrderByFeedIdDesc(interestNovelIds);

        if (interestFeeds.isEmpty()) {
            return InterestFeedsGetResponse.of(Collections.emptyList(), "NO_ASSOCIATED_FEEDS");
        }

        Set<Byte> avatarIds = interestFeeds.stream()
                .map(feed -> feed.getUser().getAvatarId())
                .collect(Collectors.toSet());
        Map<Byte, Avatar> avatarMap = avatarRepository.findAllById(avatarIds)
                .stream()
                .collect(Collectors.toMap(Avatar::getAvatarId, avatar -> avatar));

        List<InterestFeedGetResponse> interestFeedGetResponses = interestFeeds.stream()
                .map(feed -> {
                    Novel novel = novelMap.get(feed.getNovelId());
                    Avatar avatar = avatarMap.get(feed.getUser().getAvatarId());
                    return InterestFeedGetResponse.of(novel, feed.getUser(), feed, avatar);
                })
                .toList();
        return InterestFeedsGetResponse.of(interestFeedGetResponses, "");
    }

    public NovelGetResponseFeedTab getFeedsByNovel(User user, Long novelId, Long lastFeedId, int size) {
        Long userIdOrNull = user == null
                ? null
                : user.getUserId();

        Slice<Feed> feeds = feedRepository.findFeedsByNovelId(novelId, lastFeedId, userIdOrNull,
                PageRequest.of(DEFAULT_PAGE_NUMBER, size));

        List<FeedInfo> feedGetResponses = feeds.getContent().stream()
                .map(feed -> createFeedInfo(feed, user))
                .toList();

        return NovelGetResponseFeedTab.of(feeds.hasNext(), feedGetResponses);
    }

    @Transactional(readOnly = true)
    public UserFeedsGetResponse getUserFeeds(User visitor, Long ownerId, Long lastFeedId, int size) {
        User owner = userService.getUserOrException(ownerId);
        Long visitorId = visitor == null
                ? null
                : visitor.getUserId();

        if (owner.getIsProfilePublic() || isOwner(visitor, ownerId)) {
            List<Feed> feedsByNoOffsetPagination =
                    feedRepository.findFeedsByNoOffsetPagination(owner, lastFeedId, size);

            List<Long> novelIds = feedsByNoOffsetPagination.stream()
                    .map(Feed::getNovelId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Map<Long, Novel> novelMap = novelRepository.findAllById(novelIds)
                    .stream()
                    .collect(Collectors.toMap(Novel::getNovelId, novel -> novel));

            List<UserFeedGetResponse> userFeedGetResponseList = feedsByNoOffsetPagination.stream()
                    .map(feed -> UserFeedGetResponse.of(feed, novelMap.get(feed.getNovelId()), visitorId))
                    .toList();

            // TODO Slice의 hasNext()로 판단하도록 수정
            Boolean isLoadable = feedsByNoOffsetPagination.size() == size;

            return UserFeedsGetResponse.of(isLoadable, userFeedGetResponseList);
        }

        throw new CustomUserException(PRIVATE_PROFILE_STATUS, "the profile status of the user is set to private");
    }

    private static boolean isOwner(User visitor, Long ownerId) {
        //TODO 현재는 비로그인 회원인 경우
        return visitor != null && visitor.getUserId().equals(ownerId);
    }
}
