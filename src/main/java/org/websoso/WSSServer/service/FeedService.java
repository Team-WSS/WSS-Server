package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Action.DELETE;
import static org.websoso.WSSServer.domain.common.Action.UPDATE;
import static org.websoso.WSSServer.domain.common.Flag.N;
import static org.websoso.WSSServer.domain.common.Flag.Y;
import static org.websoso.WSSServer.exception.feed.FeedErrorCode.FEED_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedUpdateRequest;
import org.websoso.WSSServer.exception.feed.exception.InvalidFeedException;
import org.websoso.WSSServer.repository.FeedRepository;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final CategoryService categoryService;
    private final NovelStatisticsService novelStatisticsService;

    @Transactional
    public void createFeed(User user, FeedCreateRequest request) {
        Feed feed = Feed.builder()
                .feedContent(request.feedContent())
                .isSpoiler(request.isSpoiler() ? Y : N)
                .novelId(request.novelId())
                .user(user)
                .build();

        if (request.novelId() != null) {
            novelStatisticsService.increaseNovelFeedCount(request.novelId());
        }

        feedRepository.save(feed);
        categoryService.createCategory(feed, request.relevantCategories());
    }

    @Transactional
    public void updateFeed(User user, Long feedId, FeedUpdateRequest request) {
        Feed feed = getFeedOrException(feedId);

        feed.validateUserAuthorization(user, UPDATE);

        feed.updateFeed(request.feedContent(), request.isSpoiler() ? Y : N, request.novelId());
        categoryService.updateCategory(feed, request.relevantCategories());
    }

    @Transactional
    public void deleteFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        feed.validateUserAuthorization(user, DELETE);

        if (feed.getNovelId() != null) {
            novelStatisticsService.decreaseNovelFeedCount(feed.getNovelId());
        }

        feedRepository.delete(feed);
    }

    @Transactional
    public void likeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        String likeUserId = String.valueOf(user.getUserId());

        feed.addLike(likeUserId);
    }

    @Transactional
    public void unLikeFeed(User user, Long feedId) {
        Feed feed = getFeedOrException(feedId);

        String unLikeUserId = String.valueOf(user.getUserId());

        feed.unLike(unLikeUserId);
    }

    private Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() ->
                new InvalidFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }

}
