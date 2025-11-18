package org.websoso.WSSServer.feed.service;

import static java.lang.Boolean.TRUE;
import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;
import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.REPORT;
import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;
import static org.websoso.WSSServer.exception.error.CustomAvatarError.AVATAR_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomCategoryError.CATEGORY_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomCategoryError.INVALID_CATEGORY_FORMAT;
import static org.websoso.WSSServer.exception.error.CustomCommentError.ALREADY_REPORTED_COMMENT;
import static org.websoso.WSSServer.exception.error.CustomCommentError.COMMENT_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomFeedError.ALREADY_LIKED;
import static org.websoso.WSSServer.exception.error.CustomFeedError.ALREADY_REPORTED_FEED;
import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomFeedError.NOT_LIKED;
import static org.websoso.WSSServer.exception.error.CustomFeedError.SELF_REPORT_NOT_ALLOWED;
import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.PRIVATE_PROFILE_STATUS;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import java.util.AbstractMap;
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
import org.websoso.WSSServer.domain.common.CategoryName;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;
import org.websoso.WSSServer.domain.common.FeedGetOption;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentGetResponse;
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
import org.websoso.WSSServer.exception.error.CustomCommentError;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.exception.exception.CustomCategoryException;
import org.websoso.WSSServer.exception.exception.CustomCommentException;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.domain.Category;
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.FeedCategory;
import org.websoso.WSSServer.feed.domain.FeedImage;
import org.websoso.WSSServer.feed.domain.Like;
import org.websoso.WSSServer.feed.domain.PopularFeed;
import org.websoso.WSSServer.feed.domain.ReportedComment;
import org.websoso.WSSServer.feed.domain.ReportedFeed;
import org.websoso.WSSServer.feed.repository.CategoryRepository;
import org.websoso.WSSServer.feed.repository.CommentRepository;
import org.websoso.WSSServer.feed.repository.FeedCategoryRepository;
import org.websoso.WSSServer.feed.repository.FeedImageCustomRepository;
import org.websoso.WSSServer.feed.repository.FeedImageRepository;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.feed.repository.LikeRepository;
import org.websoso.WSSServer.feed.repository.PopularFeedRepository;
import org.websoso.WSSServer.feed.repository.ReportedCommentRepository;
import org.websoso.WSSServer.feed.repository.ReportedFeedRepository;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.repository.UserNovelRepository;
import org.websoso.WSSServer.notification.FCMClient;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.repository.NovelRepository;
import org.websoso.WSSServer.repository.AvatarRepository;
import org.websoso.WSSServer.repository.BlockRepository;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.repository.GenreRepository;
import org.websoso.WSSServer.repository.NotificationRepository;
import org.websoso.WSSServer.repository.NotificationTypeRepository;
import org.websoso.WSSServer.repository.UserRepository;
import org.websoso.WSSServer.service.DiscordMessageClient;
import org.websoso.WSSServer.service.ImageClient;
import org.websoso.WSSServer.service.MessageFormatter;

@Service
@RequiredArgsConstructor
public class FeedService {

    private static final String DEFAULT_CATEGORY = "all";
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int POPULAR_FEED_LIKE_THRESHOLD = 5;
    private static final int NOTIFICATION_TITLE_MAX_LENGTH = 12;
    private static final int NOTIFICATION_TITLE_MIN_LENGTH = 0;

    private final NovelRepository novelRepository;
    private final FeedRepository feedRepository;
    private final CategoryRepository categoryRepository;
    private final FeedCategoryRepository feedCategoryRepository;
    private final CommentRepository commentRepository;
    private final ReportedCommentRepository reportedCommentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LikeRepository likeRepository;
    private final PopularFeedRepository popularFeedRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final BlockRepository blockRepository;
    private final GenrePreferenceRepository genrePreferenceRepository;
    private final ReportedFeedRepository reportedFeedRepository;
    private final UserRepository userRepository;
    private final AvatarRepository avatarRepository;
    private final FeedImageRepository feedImageRepository;
    private final FeedImageCustomRepository feedImageCustomRepository;
    private final UserNovelRepository userNovelRepository;
    private final GenreRepository genreRepository;
    private final DiscordMessageClient discordMessageClient;
    private final ImageClient imageClient;
    private final FCMClient fcmClient;

    @Transactional
    public FeedCreateResponse createFeed(User user, FeedCreateRequest request, FeedImageCreateRequest imagesRequest) {
        List<FeedImage> feedImages = processFeedImages(imagesRequest.images());

        Optional.ofNullable(request.novelId()).ifPresent(novelId -> novelRepository.findById(novelId)
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found")));
        Feed feed = Feed.create(request.feedContent(), request.novelId(), request.isSpoiler(), request.isPublic(), user,
                feedImages);

        feedRepository.save(feed);
        for (String relevantCategory : request.relevantCategories()) {
            Category category = findCategoryByName(relevantCategory);
            feedCategoryRepository.save(FeedCategory.create(feed, category));
        }

        return FeedCreateResponse.of(feedImages);
    }

    @Transactional
    public FeedCreateResponse updateFeed(Long feedId, FeedUpdateRequest request, FeedImageUpdateRequest imagesRequest) {
        Feed feed = getFeedOrException(feedId);

        List<FeedImage> oldImages = new ArrayList<>(feed.getImages());

        if (request.novelId() != null && feed.isNovelChanged(request.novelId())) {
            novelRepository.findById(request.novelId())
                    .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND,
                            "novel with the given id is not found"));
        }

        List<FeedImage> feedImages = processFeedImages(imagesRequest.images());

        feed.updateFeed(request.feedContent(), request.isSpoiler(), request.isPublic(), request.novelId(), feedImages);
        List<FeedCategory> feedCategories = feedCategoryRepository.findByFeed(feed);

        if (feedCategories.isEmpty()) {
            throw new CustomCategoryException(CATEGORY_NOT_FOUND, "Category for the given feed was not found");
        }

        Set<Category> categories = feedCategories.stream().map(FeedCategory::getCategory).collect(Collectors.toSet());

        Set<Category> newCategories = request.relevantCategories().stream().map(this::findCategoryByName)
                .collect(Collectors.toSet());

        for (Category newCategory : newCategories) {
            if (categories.contains(newCategory)) {
                categories.remove(newCategory);
            } else {
                feedCategoryRepository.save(FeedCategory.create(feed, newCategory));
            }
        }

        for (Category category : categories) {
            feedCategoryRepository.deleteByCategoryAndFeed(category, feed);
        }

        List<String> oldImageUrls = oldImages.stream().map(FeedImage::getUrl).toList();
        eventPublisher.publishEvent(new FeedImageDeleteEvent(oldImageUrls));

        return FeedCreateResponse.of(feedImages);
    }

    private List<FeedImage> processFeedImages(List<MultipartFile> images) {
        List<String> uploadedImageUrls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            try {
                for (MultipartFile image : images) {
                    String imageUrl = imageClient.uploadFeedImage(image);
                    uploadedImageUrls.add(imageUrl);
                }
            } catch (Exception e) {
                if (!uploadedImageUrls.isEmpty()) {
                    imageClient.deleteImages(uploadedImageUrls);
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

    @Transactional
    public void deleteFeed(Long feedId) {
        List<Long> commentIds = commentRepository.findAllByFeedId(feedId).stream().map(Comment::getCommentId).toList();
        reportedCommentRepository.deleteByCommentIdsIn(commentIds);
        feedRepository.deleteById(feedId);
    }

    @Transactional
    public void likeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        if (likeRepository.existsByUserIdAndFeed(user.getUserId(), feed)) {
            throw new CustomFeedException(ALREADY_LIKED, "user already liked that feed");
        }
        likeRepository.save(Like.create(user.getUserId(), feed));

        if (feed.getLikes().size() == POPULAR_FEED_LIKE_THRESHOLD) {
            if (!popularFeedRepository.existsByFeed(feed)) {
                popularFeedRepository.save(PopularFeed.create(feed));

                sendPopularFeedPushMessage(feed);
            }
        }

        sendLikePushMessage(user, feed);
    }

    private void sendLikePushMessage(User liker, Feed feed) {
        User feedOwner = feed.getUser();
        if (liker.equals(feedOwner) || blockRepository.existsByBlockingIdAndBlockedId(feedOwner.getUserId(),
                liker.getUserId())) {
            return;
        }

        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("좋아요");

        String notificationTitle = createNotificationTitle(feed);
        String notificationBody = String.format("%s님이 내 글을 좋아해요.", liker.getNickname());
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

    @Transactional
    public void unLikeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);
        Like like = likeRepository.findByUserIdAndFeed(user.getUserId(), feed)
                .orElseThrow(() -> new CustomFeedException(NOT_LIKED,
                        "User did not like this feed or like already deleted"));
        likeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public FeedGetResponse getFeedById(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);
        UserBasicInfo feedUserBasicInfo = getUserBasicInfo(feed.getUser());
        Novel novel = getLinkedNovelOrNull(feed.getNovelId());
        Boolean isLiked = isUserLikedFeed(user, feed);
        List<String> relevantCategories = feed.getFeedCategories().stream()
                .map(feedCategory -> feedCategory.getCategory().getCategoryName().getLabel())
                .collect(Collectors.toList());
        Boolean isMyFeed = isUserFeedOwner(feed.getUser(), user);

        return FeedGetResponse.of(feed, feedUserBasicInfo, novel, isLiked, relevantCategories, isMyFeed);
    }

    @Transactional(readOnly = true)
    public FeedsGetResponse getFeeds(User user, String category, Long lastFeedId, int size,
                                     FeedGetOption feedGetOption) {
        Long userIdOrNull = Optional.ofNullable(user).map(User::getUserId).orElse(null);

        List<Genre> genres = getPreferenceGenres(user);

        Slice<Feed> feeds = findFeedsByCategoryLabel(getChosenCategoryOrDefault(category), lastFeedId, userIdOrNull,
                PageRequest.of(DEFAULT_PAGE_NUMBER, size), feedGetOption, genres);

        List<FeedInfo> feedGetResponses = feeds.getContent().stream().filter(feed -> feed.isVisibleTo(userIdOrNull))
                .map(feed -> createFeedInfo(feed, user)).toList();

        return FeedsGetResponse.of(getChosenCategoryOrDefault(category), feeds.hasNext(), feedGetResponses);
    }

    private List<Genre> getPreferenceGenres(User user) {
        if (user == null) {
            return null;
        }
        return genrePreferenceRepository.findByUser(user).stream().map(GenrePreference::getGenre).toList();
    }

    private static String getChosenCategoryOrDefault(String category) {
        return Optional.ofNullable(category).orElse(DEFAULT_CATEGORY);
    }

    // ToDO: FeedController -> FeedService를 통해 Comment를 생성하는 로직에서 FeedController -> CommentService를 통해 Comment를 생성하는 로직으로 수정필요
    @Transactional
    public void createComment(User user, Long feedId, CommentCreateRequest request) {
        Feed feed = getFeedOrException(feedId);
        commentRepository.save(Comment.create(user.getUserId(), feed, request.commentContent()));
        sendCommentPushMessageToFeedOwner(user, feed);
        sendCommentPushMessageToCommenters(user, feed);
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

    private UserBasicInfo getUserBasicInfo(User user) {
        return user.getUserBasicInfo(
                avatarRepository.findById(user.getAvatarId()).orElseThrow(() ->
                                new CustomAvatarException(AVATAR_NOT_FOUND, "avatar with the given id was not found"))
                        .getAvatarImage());
    }

    private Novel getLinkedNovelOrNull(Long linkedNovelId) {
        if (linkedNovelId == null) {
            return null;
        }
        return novelRepository.findById(linkedNovelId)
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND,
                        "novel with the given id is not found"));
    }

    private Boolean isUserLikedFeed(User user, Feed feed) {
        return likeRepository.existsByUserIdAndFeed(user.getUserId(), feed);
    }

    private Boolean isUserFeedOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private FeedInfo createFeedInfo(Feed feed, User user) {
        UserBasicInfo userBasicInfo = getUserBasicInfo(feed.getUser());
        Novel novel = getLinkedNovelOrNull(feed.getNovelId());
        Boolean isLiked = user != null && isUserLikedFeed(user, feed);
        List<String> relevantCategories = feed.getFeedCategories().stream()
                .map(feedCategory -> feedCategory.getCategory().getCategoryName().getLabel())
                .collect(Collectors.toList());
        Boolean isMyFeed = user != null && isUserFeedOwner(feed.getUser(), user);
        Integer imageCount = feedImageRepository.countByFeedId(feed.getFeedId());
        Optional<FeedImage> thumbnailImage = feedImageCustomRepository.findThumbnailFeedImageByFeedId(feed.getFeedId());
        String thumbnailUrl = thumbnailImage.map(FeedImage::getUrl).orElse(null);

        return FeedInfo.of(feed, userBasicInfo, novel, isLiked, relevantCategories, isMyFeed, thumbnailUrl, imageCount,
                user);
    }

    private Slice<Feed> findFeedsByCategoryLabel(String category, Long lastFeedId, Long userId, PageRequest pageRequest,
                                                 FeedGetOption feedGetOption, List<Genre> genres) {
        if (DEFAULT_CATEGORY.equals(category)) {
            if (FeedGetOption.isAll(feedGetOption)) {
                return feedRepository.findFeeds(lastFeedId, userId, pageRequest);
            } else {
                return feedRepository.findRecommendedFeeds(lastFeedId, userId, pageRequest, genres);
            }
        } else {
            if (FeedGetOption.isAll(feedGetOption)) {
                return feedCategoryRepository.findFeedsByCategory(findCategoryByName(category), lastFeedId, userId,
                        pageRequest);
            } else {
                return feedCategoryRepository.findRecommendedFeedsByCategoryLabel(findCategoryByName(category),
                        lastFeedId,
                        userId, pageRequest, genres);
            }
        }
    }

    @Transactional(readOnly = true)
    public InterestFeedsGetResponse getInterestFeeds(User user) {
        List<Novel> interestNovels = userNovelRepository.findByUserAndIsInterestTrue(user).stream()
                .map(UserNovel::getNovel).toList();

        if (interestNovels.isEmpty()) {
            return InterestFeedsGetResponse.of(Collections.emptyList(), "NO_INTEREST_NOVELS");
        }

        Map<Long, Novel> novelMap = interestNovels.stream()
                .collect(Collectors.toMap(Novel::getNovelId, novel -> novel));
        List<Long> interestNovelIds = new ArrayList<>(novelMap.keySet());

        List<Feed> interestFeeds = feedRepository.findTop10ByNovelIdInOrderByFeedIdDesc(interestNovelIds);

        if (interestFeeds.isEmpty()) {
            return InterestFeedsGetResponse.of(Collections.emptyList(), "NO_ASSOCIATED_FEEDS");
        }

        Set<Byte> avatarIds = interestFeeds.stream().map(feed -> feed.getUser().getAvatarId())
                .collect(Collectors.toSet());
        Map<Byte, Avatar> avatarMap = avatarRepository.findAllById(avatarIds).stream()
                .collect(Collectors.toMap(Avatar::getAvatarId, avatar -> avatar));

        List<InterestFeedGetResponse> interestFeedGetResponses = interestFeeds.stream()
                .filter(feed -> feed.isVisibleTo(user.getUserId())).map(feed -> {
                    Novel novel = novelMap.get(feed.getNovelId());
                    Avatar avatar = avatarMap.get(feed.getUser().getAvatarId());
                    return InterestFeedGetResponse.of(novel, feed.getUser(), feed, avatar);
                }).toList();
        return InterestFeedsGetResponse.of(interestFeedGetResponses, "");
    }

    @Transactional(readOnly = true)
    public NovelGetResponseFeedTab getFeedsByNovel(User user, Long novelId, Long lastFeedId, int size) {
        Long userIdOrNull = Optional.ofNullable(user).map(User::getUserId).orElse(null);
        Slice<Feed> feeds = feedRepository.findFeedsByNovelId(novelId, lastFeedId, userIdOrNull,
                PageRequest.of(DEFAULT_PAGE_NUMBER, size));

        List<FeedInfo> feedGetResponses = feeds.getContent().stream().filter(feed -> feed.isVisibleTo(userIdOrNull))
                .map(feed -> createFeedInfo(feed, user)).toList();

        return NovelGetResponseFeedTab.of(feeds.hasNext(), feedGetResponses);
    }

    @Transactional(readOnly = true)
    public UserFeedsGetResponse getUserFeeds(User visitor, Long ownerId, Long lastFeedId, int size, Boolean isVisible,
                                             Boolean isUnVisible, List<String> genreNames, SortCriteria sortCriteria) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));
        Long visitorId = Optional.ofNullable(visitor).map(User::getUserId).orElse(null);

        if (owner.getIsProfilePublic() || isOwner(visitor, ownerId)) {
            List<Genre> genres = getGenres(genreNames);

            List<Feed> visibleFeeds = feedRepository.findFeedsByNoOffsetPagination(owner, lastFeedId, size, isVisible,
                    isUnVisible, sortCriteria, genres, visitorId);

            List<Long> novelIds = visibleFeeds.stream().map(Feed::getNovelId).filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Map<Long, Novel> novelMap = novelRepository.findAllById(novelIds).stream()
                    .collect(Collectors.toMap(Novel::getNovelId, novel -> novel));

            List<UserFeedGetResponse> userFeedGetResponseList = visibleFeeds.stream()
                    .map(feed -> UserFeedGetResponse.of(feed, novelMap.get(feed.getNovelId()), visitorId,
                            getThumbnailUrl(feed), getImageCount(feed))).toList();

            // TODO Slice의 hasNext()로 판단하도록 수정
            Boolean isLoadable = visibleFeeds.size() == size;
            long feedsCount = feedRepository.countVisibleFeeds(owner, lastFeedId, isVisible, isUnVisible, genres,
                    visitorId);

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
                    .map(genreName -> genreRepository.findByGenreName(genreName)
                            .orElseThrow(() -> new CustomGenreException(GENRE_NOT_FOUND,
                                    "genre with the given name is not found"))
                    ).toList();
        }
        return null;
    }

    private String getThumbnailUrl(Feed feed) {
        Optional<FeedImage> thumbnailImage = feedImageCustomRepository.findThumbnailFeedImageByFeedId(feed.getFeedId());
        return thumbnailImage.map(FeedImage::getUrl).orElse(null);
    }

    private Integer getImageCount(Feed feed) {
        return feedImageRepository.countByFeedId(feed.getFeedId());
    }

    private Category findCategoryByName(String categoryName) {
        return categoryRepository.findByCategoryName(CategoryName.valueOf(categoryName)).orElseThrow(
                () -> new CustomCategoryException(INVALID_CATEGORY_FORMAT,
                        "Category for the given feed was not found"));
    }

    private void sendPopularFeedPushMessage(Feed feed) {
        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("지금뜨는글");

        User feedOwner = feed.getUser();
        Long feedId = feed.getFeedId();
        String notificationTitle = "지금 뜨는 글 등극\uD83D\uDE4C";
        String notificationBody = createNotificationBody(feed);

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

    private String createNotificationBody(Feed feed) {
        return String.format("내가 남긴 %s 글이 관심 받고 있어요!", generateNotificationBodyFragment(feed));
    }

    private String generateNotificationBodyFragment(Feed feed) {
        if (feed.getNovelId() == null) {
            String feedContent = feed.getFeedContent();
            feedContent = feedContent.length() <= 12
                    ? feedContent
                    : feedContent.substring(0, 12);
            return "'" + feedContent + "...'";
        }
        Novel novel = novelRepository.findById(feed.getNovelId())
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND,
                        "novel with the given id is not found"));
        return String.format("<%s>", novel.getTitle());
    }

    //CommentService 내용
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

    private Boolean isUserCommentOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private Boolean isBlocked(User user, User createdFeedUser) {
        return blockRepository.existsByBlockingIdAndBlockedId(user.getUserId(), createdFeedUser.getUserId());
    }

}
