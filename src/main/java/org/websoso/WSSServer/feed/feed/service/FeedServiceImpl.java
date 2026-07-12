package org.websoso.WSSServer.feed.feed.service;

import static org.websoso.WSSServer.feed.feed.exception.CustomFeedError.FEED_NOT_FOUND;
import static org.websoso.WSSServer.feed.feed.exception.CustomFeedError.HIDDEN_FEED_ACCESS;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_AUTHORIZED;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.feed.feed.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.feed.repository.FeedRepository;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl {

    private static final int DEFAULT_PAGE_NUMBER = 0;

    private final FeedRepository feedRepository;

    @Transactional
    public void createFeed(Feed feed) {
        feedRepository.save(feed);
    }

    @Transactional
    public void delete(Feed feed) {
        feedRepository.delete(feed);
    }

    @Transactional
    public void updateWriterToUnknown(Long writerId) {
        feedRepository.updateUserToUnknown(writerId);
    }

    @Transactional(readOnly = true)
    public Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }

    @Transactional(readOnly = true)
    public Feed getOwnedFeedOrException(Long feedId, Long userId) {
        Feed feed = getFeedOrException(feedId);

        if (!feed.isMine(userId)) {
            throw new CustomUserException(INVALID_AUTHORIZED, "User with ID " + userId + " is not the owner of feed " + feed.getFeedId());
        }

        return feed;
    }

    @Transactional(readOnly = true)
    public Feed getAccessFeedOrException(Long feedId, Long userId) {
        Feed feed = getFeedOrException(feedId);

        if (!feed.canAccess(userId)) {
            throw new CustomFeedException(HIDDEN_FEED_ACCESS, "Cannot access hidden feed.");
        }

        return feed;
    }

    @Transactional(readOnly = true)
    public Slice<Feed> findFeedsByNovel(Long userIdOrNull, Long novelId, Long lastFeedId, int size) {
        return feedRepository.findFeedsByNovelId(novelId, lastFeedId, userIdOrNull,
                PageRequest.of(DEFAULT_PAGE_NUMBER, size));
    }

    @Transactional(readOnly = true)
    public Slice<Feed> getViewableUserFeed(User owner, Long lastFeedId, int size, Boolean isVisible,
                                           Boolean isUnVisible, SortCriteria sortCriteria,
                                           List<Genre> genres, Long visitorId, boolean isNotNovelConnect) {
        return feedRepository.findFeedsByNoOffsetPagination(owner, lastFeedId, size, isVisible,
                isUnVisible, sortCriteria, genres, visitorId, isNotNovelConnect);
    }

    @Transactional(readOnly = true)
    public long getViewableUserFeedCount(User owner, Boolean isVisible,
                                         Boolean isUnVisible, List<Genre> genres,
                                         Long visitorId, boolean includeEtc) {
        return feedRepository.countVisibleFeeds(owner, isVisible, isUnVisible, genres, visitorId, includeEtc);
    }

    @Transactional(readOnly = true)
    public Slice<Feed> findFeeds(Long lastFeedId, Long userId, PageRequest pageRequest, List<Long> blockedUserIds) {
        return feedRepository.findFeeds(lastFeedId, userId, pageRequest, blockedUserIds);
    }

    @Transactional(readOnly = true)
    public Slice<Feed> findRecommendedFeeds(Long lastFeedId, Long userId, PageRequest pageRequest, List<Genre> preferenceGenres, List<Long> blockedUserIds) {
        return feedRepository.findRecommendedFeeds(lastFeedId, userId, pageRequest, preferenceGenres, blockedUserIds);
    }

}
