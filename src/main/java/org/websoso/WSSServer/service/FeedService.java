package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;
import static org.websoso.WSSServer.exception.error.CustomFeedError.BLOCKED_USER_ACCESS;
import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomFeedError.HIDDEN_FEED_ACCESS;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.User.UserBasicInfo;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedGetResponse;
import org.websoso.WSSServer.dto.feed.FeedUpdateRequest;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.repository.FeedRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedService {

    private static final String LIKE_USER_PATTERN = "{%s}";

    private final FeedRepository feedRepository;
    private final CategoryService categoryService;
    private final NovelStatisticsService novelStatisticsService;
    private final NovelService novelService;
    private final AvatarService avatarService;
    private final BlockService blockService;

    public void createFeed(User user, FeedCreateRequest request) {
        Feed feed = Feed.builder()
                .feedContent(request.feedContent())
                .isSpoiler(request.isSpoiler())
                .novelId(request.novelId())
                .user(user)
                .build();

        if (request.novelId() != null) {
            novelStatisticsService.increaseNovelFeedCount(novelService.getNovelOrException(request.novelId()));
        }

        feedRepository.save(feed);
        categoryService.createCategory(feed, request.relevantCategories());
    }

    public void updateFeed(User user, Long feedId, FeedUpdateRequest request) {
        Feed feed = getFeedOrException(feedId);

        feed.validateUserAuthorization(user, UPDATE);

        if (feed.isNovelChanged(request.novelId())) {
            if (feed.isNovelLinked()) {
                novelStatisticsService.decreaseNovelFeedCount(novelService.getNovelOrException(feed.getNovelId()));
            }
            if (request.novelId() != null) {
                novelStatisticsService.increaseNovelFeedCount(novelService.getNovelOrException(request.novelId()));
            }
        }

        feed.updateFeed(request.feedContent(), request.isSpoiler(), request.novelId());
        categoryService.updateCategory(feed, request.relevantCategories());
    }

    public void deleteFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        feed.validateUserAuthorization(user, DELETE);

        if (feed.getNovelId() != null) {
            novelStatisticsService.decreaseNovelFeedCount(novelService.getNovelOrException(feed.getNovelId()));
        }

        feedRepository.delete(feed);
    }

    public void likeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        String likeUserId = String.valueOf(user.getUserId());

        feed.addLike(likeUserId);
    }

    public void unLikeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        String unLikeUserId = String.valueOf(user.getUserId());

        feed.unLike(unLikeUserId);
    }

    @Transactional(readOnly = true)
    public FeedGetResponse getFeedById(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        isHiddenFeed(feed);
        isBlockedRelationship(feed.getUser(), user);

        UserBasicInfo userBasicInfo = getUserBasicInfo(feed.getUser());
        Novel novel = getLinkedNovelOrNull(feed.getNovelId());
        Boolean isLiked = isUserLikedFeed(feed.getLikeUsers(), user);
        List<String> relevantCategories = categoryService.getRelevantCategoryNames(feed.getCategory());
        Boolean isMyFeed = isUserFeedOwner(feed.getUser(), user);

        return FeedGetResponse.of(feed, userBasicInfo, novel, isLiked, relevantCategories, isMyFeed);
    }

    private Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() ->
                new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }

    private void isHiddenFeed(Feed feed) {
        if (feed.getIsHidden()) {
            throw new CustomFeedException(HIDDEN_FEED_ACCESS, "Cannot access hidden feed.");
        }
    }

    private void isBlockedRelationship(User createdFeedUser, User user) {
        if (blockService.isBlockedRelationship(user.getUserId(), createdFeedUser.getUserId())) {
            throw new CustomFeedException(BLOCKED_USER_ACCESS,
                    "cannot access this feed because either you or the feed author has blocked the other.");
        }
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

    private Boolean isUserLikedFeed(String likeUsers, User user) {
        String formattedLikeUser = String.format(LIKE_USER_PATTERN, user.getUserId());
        return likeUsers.contains(formattedLikeUser);
    }

    private Boolean isUserFeedOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

}
