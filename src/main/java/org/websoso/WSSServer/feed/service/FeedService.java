package org.websoso.WSSServer.feed.service;

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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.NotificationType;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserDevice;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;
import org.websoso.WSSServer.domain.common.FeedGetOption;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentUpdateRequest;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedCreateResponse;
import org.websoso.WSSServer.dto.feed.FeedGetResponse;
import org.websoso.WSSServer.dto.feed.FeedImageCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedImageDeleteEvent;
import org.websoso.WSSServer.dto.feed.FeedImageUpdateRequest;
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
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.FeedImage;
import org.websoso.WSSServer.feed.repository.CommentRepository;
import org.websoso.WSSServer.feed.repository.FeedImageCustomRepository;
import org.websoso.WSSServer.feed.repository.FeedImageRepository;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.feed.repository.ReportedCommentRepository;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.repository.UserNovelRepository;
import org.websoso.WSSServer.notification.FCMClient;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.repository.NovelRepository;
import org.websoso.WSSServer.novel.service.NovelService;
import org.websoso.WSSServer.repository.AvatarRepository;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.repository.NotificationRepository;
import org.websoso.WSSServer.repository.NotificationTypeRepository;
import org.websoso.WSSServer.service.AvatarService;
import org.websoso.WSSServer.service.BlockService;
import org.websoso.WSSServer.service.GenreService;
import org.websoso.WSSServer.service.ImageService;
import org.websoso.WSSServer.service.MessageFormatter;
import org.websoso.WSSServer.service.MessageService;
import org.websoso.WSSServer.service.UserService;

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
    private final ImageService imageService;
    private final FeedImageCustomRepository feedImageCustomRepository;
    private final UserNovelRepository userNovelRepository;
    private final AvatarRepository avatarRepository;
    private final CommentService commentService;
    private final ReportedFeedService reportedFeedService;
    private final MessageService messageService;
    private final UserService userService;
    private final NovelRepository novelRepository;
    private final FCMClient fcmClient;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FeedImageRepository feedImageRepository;
    private final GenreService genreService;
    private final GenrePreferenceRepository genrePreferenceRepository;
    private final CommentRepository commentRepository;
    private final ReportedCommentRepository reportedCommentRepository;

    public FeedCreateResponse createFeed(User user, FeedCreateRequest request, FeedImageCreateRequest imagesRequest) {
        List<FeedImage> feedImages = processFeedImages(imagesRequest.images());

        Optional.ofNullable(request.novelId())
                .ifPresent(novelService::getNovelOrException);
        Feed feed = Feed.create(
                request.feedContent(),
                request.novelId(),
                request.isSpoiler(),
                request.isPublic(),
                user,
                feedImages);
        feedRepository.save(feed);
        feedCategoryService.createFeedCategory(feed, request.relevantCategories());

        return FeedCreateResponse.of(feedImages);
    }

    public FeedCreateResponse updateFeed(Long feedId, FeedUpdateRequest request, FeedImageUpdateRequest imagesRequest) {
        Feed feed = getFeedOrException(feedId);

        List<FeedImage> oldImages = new ArrayList<>(feed.getImages());

        if (request.novelId() != null && feed.isNovelChanged(request.novelId())) {
            novelService.getNovelOrException(request.novelId());
        }

        List<FeedImage> feedImages = processFeedImages(imagesRequest.images());

        feed.updateFeed(
                request.feedContent(),
                request.isSpoiler(),
                request.isPublic(),
                request.novelId(),
                feedImages);
        feedCategoryService.updateFeedCategory(feed, request.relevantCategories());

        List<String> oldImageUrls = oldImages.stream()
                .map(FeedImage::getUrl)
                .toList();
        eventPublisher.publishEvent(new FeedImageDeleteEvent(oldImageUrls));

        return FeedCreateResponse.of(feedImages);
    }

    private List<FeedImage> processFeedImages(List<MultipartFile> images) {
        List<String> uploadedImageUrls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            try {
                for (MultipartFile image : images) {
                    String imageUrl = imageService.uploadFeedImage(image);
                    uploadedImageUrls.add(imageUrl);
                }
            } catch (Exception e) {
                if (!uploadedImageUrls.isEmpty()) {
                    imageService.deleteImages(uploadedImageUrls);
                }

                throw e;
            }
        }

        List<FeedImage> feedImages = new ArrayList<>();
        if (!uploadedImageUrls.isEmpty()) {
            feedImages.add(FeedImage.createThumbnail(uploadedImageUrls.get(0)));
            for (int i = 1; i < uploadedImageUrls.size(); i++) {
                feedImages.add(FeedImage.createCommon(uploadedImageUrls.get(i), i));
            }
        }

        return feedImages;
    }

    public void deleteFeed(Long feedId) {
        List<Long> commentIds = commentRepository.findAllByFeedId(feedId).stream()
                .map(Comment::getCommentId).toList();
        reportedCommentRepository.deleteByCommentIdsIn(commentIds);
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
        String notificationBody = String.format("%s님이 내 글을 좋아해요.", liker.getNickname());
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
        fcmClient.sendMulticastPushMessage(
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
        UserBasicInfo feedUserBasicInfo = getUserBasicInfo(feed.getUser());
        Novel novel = getLinkedNovelOrNull(feed.getNovelId());
        Boolean isLiked = isUserLikedFeed(user, feed);
        List<String> relevantCategories = feedCategoryService.getRelevantCategoryNames(feed.getFeedCategories());
        Boolean isMyFeed = isUserFeedOwner(feed.getUser(), user);

        return FeedGetResponse.of(feed, feedUserBasicInfo, novel, isLiked, relevantCategories, isMyFeed);
    }

    @Transactional(readOnly = true)
    public FeedsGetResponse getFeeds(User user, String category, Long lastFeedId, int size,
                                     FeedGetOption feedGetOption) {
        Long userIdOrNull = Optional.ofNullable(user)
                .map(User::getUserId)
                .orElse(null);

        List<Genre> genres = getPreferenceGenres(user);

        Slice<Feed> feeds = findFeedsByCategoryLabel(getChosenCategoryOrDefault(category),
                lastFeedId, userIdOrNull, PageRequest.of(DEFAULT_PAGE_NUMBER, size), feedGetOption, genres);

        List<FeedInfo> feedGetResponses = feeds.getContent()
                .stream()
                .filter(feed -> feed.isVisibleTo(userIdOrNull))
                .map(feed -> createFeedInfo(feed, user))
                .toList();

        return FeedsGetResponse.of(getChosenCategoryOrDefault(category), feeds.hasNext(), feedGetResponses);
    }

    private List<Genre> getPreferenceGenres(User user) {
        if (user == null) {
            return null;
        }
        return genrePreferenceRepository.findByUser(user).stream()
                .map(GenrePreference::getGenre)
                .toList();
    }

    private static String getChosenCategoryOrDefault(String category) {
        return Optional.ofNullable(category)
                .orElse(DEFAULT_CATEGORY);
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
        Integer imageCount = feedImageRepository.countByFeedId(feed.getFeedId());
        Optional<FeedImage> thumbnailImage = feedImageCustomRepository.findThumbnailFeedImageByFeedId(feed.getFeedId());
        String thumbnailUrl = thumbnailImage.map(FeedImage::getUrl).orElse(null);

        return FeedInfo.of(feed, userBasicInfo, novel, isLiked, relevantCategories, isMyFeed, thumbnailUrl, imageCount,
                user);
    }

    private Slice<Feed> findFeedsByCategoryLabel(String category, Long lastFeedId, Long userId,
                                                 PageRequest pageRequest, FeedGetOption feedGetOption,
                                                 List<Genre> genres) {
        if (DEFAULT_CATEGORY.equals(category)) {
            if (FeedGetOption.isAll(feedGetOption)) {
                return feedRepository.findFeeds(lastFeedId, userId, pageRequest);
            } else {
                return feedRepository.findRecommendedFeeds(lastFeedId, userId, pageRequest, genres);
            }
        } else {
            if (FeedGetOption.isAll(feedGetOption)) {
                return feedCategoryService.getFeedsByCategoryLabel(category, lastFeedId, userId, pageRequest);
            } else {
                return feedCategoryService.getRecommendedFeedsByCategoryLabel(category, lastFeedId, userId, pageRequest,
                        genres);
            }
        }
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
                .filter(feed -> feed.isVisibleTo(user.getUserId()))
                .map(feed -> {
                    Novel novel = novelMap.get(feed.getNovelId());
                    Avatar avatar = avatarMap.get(feed.getUser().getAvatarId());
                    return InterestFeedGetResponse.of(novel, feed.getUser(), feed, avatar);
                })
                .toList();
        return InterestFeedsGetResponse.of(interestFeedGetResponses, "");
    }

    public NovelGetResponseFeedTab getFeedsByNovel(User user, Long novelId, Long lastFeedId, int size) {
        Long userIdOrNull = Optional.ofNullable(user)
                .map(User::getUserId)
                .orElse(null);
        Slice<Feed> feeds = feedRepository.findFeedsByNovelId(novelId, lastFeedId, userIdOrNull,
                PageRequest.of(DEFAULT_PAGE_NUMBER, size));

        List<FeedInfo> feedGetResponses = feeds.getContent()
                .stream()
                .filter(feed -> feed.isVisibleTo(userIdOrNull))
                .map(feed -> createFeedInfo(feed, user))
                .toList();

        return NovelGetResponseFeedTab.of(feeds.hasNext(), feedGetResponses);
    }

    @Transactional(readOnly = true)
    public UserFeedsGetResponse getUserFeeds(User visitor, Long ownerId, Long lastFeedId, int size, Boolean isVisible,
                                             Boolean isUnVisible, List<String> genreNames,
                                             SortCriteria sortCriteria) {
        User owner = userService.getUserOrException(ownerId);
        Long visitorId = Optional.ofNullable(visitor)
                .map(User::getUserId)
                .orElse(null);

        if (owner.getIsProfilePublic() || isOwner(visitor, ownerId)) {
            List<Genre> genres = getGenres(genreNames);

            List<Feed> visibleFeeds = feedRepository.findFeedsByNoOffsetPagination(owner, lastFeedId, size, isVisible,
                    isUnVisible, sortCriteria, genres, visitorId);

            List<Long> novelIds = visibleFeeds.stream()
                    .map(Feed::getNovelId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Map<Long, Novel> novelMap = novelRepository.findAllById(novelIds)
                    .stream()
                    .collect(Collectors.toMap(Novel::getNovelId, novel -> novel));

            List<UserFeedGetResponse> userFeedGetResponseList = visibleFeeds.stream()
                    .map(feed -> UserFeedGetResponse.of(feed, novelMap.get(feed.getNovelId()), visitorId,
                            getThumbnailUrl(feed),
                            getImageCount(feed)))
                    .toList();

            // TODO Slice의 hasNext()로 판단하도록 수정
            Boolean isLoadable = visibleFeeds.size() == size;
            long feedsCount = feedRepository.countVisibleFeeds(owner, lastFeedId, isVisible,
                    isUnVisible, genres, visitorId);

            return UserFeedsGetResponse.of(isLoadable, feedsCount, userFeedGetResponseList);
        }

        throw new CustomUserException(PRIVATE_PROFILE_STATUS, "the profile status of the user is set to private");
    }

    private static boolean isOwner(User visitor, Long ownerId) {
        //TODO 현재는 비로그인 회원인 경우
        return visitor != null && visitor.getUserId().equals(ownerId);
    }

    private List<Genre> getGenres(List<String> genreNames) {
        if (genreNames != null && !genreNames.isEmpty()) {
            return genreNames.stream()
                    .map(genreService::getGenreOrException)
                    .toList();
        }
        return null;
    }

    private String getThumbnailUrl(Feed feed) {
        Optional<FeedImage> thumbnailImage = feedImageCustomRepository.findThumbnailFeedImageByFeedId(
                feed.getFeedId());
        return thumbnailImage.map(FeedImage::getUrl).orElse(null);
    }

    private Integer getImageCount(Feed feed) {
        return feedImageRepository.countByFeedId(feed.getFeedId());
    }
}
