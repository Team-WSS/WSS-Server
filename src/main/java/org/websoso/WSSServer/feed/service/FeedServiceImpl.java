package org.websoso.WSSServer.feed.service;

import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.common.FeedGetOption;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.FeedImage;
import org.websoso.WSSServer.feed.domain.PopularFeed;
import org.websoso.WSSServer.feed.repository.FeedImageCustomRepository;
import org.websoso.WSSServer.feed.repository.FeedImageRepository;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.feed.repository.PopularFeedRepository;
import org.websoso.WSSServer.repository.GenreRepository;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl {

    private final FeedRepository feedRepository;
    private final FeedImageRepository feedImageRepository;
    private final FeedImageCustomRepository feedImageCustomRepository;
    private final PopularFeedRepository popularFeedRepository;
    private final GenreRepository genreRepository;

    private static final String DEFAULT_CATEGORY = "all";

    @Transactional(readOnly = true)
    public Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }

    @Transactional(readOnly = true)
    public Slice<Feed> findFeedsByCategoryLabel(String category, Long lastFeedId, Long userId, PageRequest pageRequest,
                                                FeedGetOption feedGetOption, List<Genre> preferenceGenres) {
        if (DEFAULT_CATEGORY.equals(category)) {
            if (FeedGetOption.isAll(feedGetOption)) {
                return feedRepository.findFeeds(lastFeedId, userId, pageRequest);
            } else {
                return feedRepository.findRecommendedFeeds(lastFeedId, userId, pageRequest, preferenceGenres);
            }
        }

        boolean includeEtc = "etc".equals(category);
        List<Genre> filterGenres = includeEtc ? null : List.of(findGenreByName(category));

        if (FeedGetOption.isAll(feedGetOption)) {
            return feedRepository.findFeedsByGenres(filterGenres, includeEtc, lastFeedId, userId, pageRequest);
        } else {
            return feedRepository.findRecommendedFeeds(lastFeedId, userId, pageRequest, filterGenres);
        }
    }

    private Genre findGenreByName(String genreName) {
        return genreRepository.findByGenreName(genreName)
                .orElseThrow(() -> new CustomGenreException(GENRE_NOT_FOUND,
                        "genre with the given name is not found"));
    }

    @Transactional(readOnly = true)
    public Integer countByFeedId(Long feedId) {
        return feedImageRepository.countByFeedId(feedId);
    }

    @Transactional(readOnly = true)
    public Optional<FeedImage> findThumbnailFeedImageByFeedId(Long feedId) {
        return feedImageCustomRepository.findThumbnailFeedImageByFeedId(feedId);
    }

    @Transactional(readOnly = true)
    public List<PopularFeed> findPopularFeedsWithUser(Long userId) {
        return popularFeedRepository.findTodayPopularFeeds(userId);
    }

    @Transactional(readOnly = true)
    public List<PopularFeed> findPopularFeedsWithoutUser() {
        return popularFeedRepository.findTop9ByOrderByPopularFeedIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Feed> findInterestFeeds(List<Long> interestNovelIds) {
        return feedRepository.findTop10ByNovelIdInOrderByFeedIdDesc(interestNovelIds);
    }
}