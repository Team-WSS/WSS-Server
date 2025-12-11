package org.websoso.WSSServer.feed.service;

import static org.websoso.WSSServer.exception.error.CustomCategoryError.INVALID_CATEGORY_FORMAT;
import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.common.CategoryName;
import org.websoso.WSSServer.domain.common.FeedGetOption;
import org.websoso.WSSServer.exception.exception.CustomCategoryException;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.feed.domain.Category;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.FeedImage;
import org.websoso.WSSServer.feed.domain.PopularFeed;
import org.websoso.WSSServer.feed.repository.CategoryRepository;
import org.websoso.WSSServer.feed.repository.FeedCategoryRepository;
import org.websoso.WSSServer.feed.repository.FeedImageCustomRepository;
import org.websoso.WSSServer.feed.repository.FeedImageRepository;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.feed.repository.PopularFeedRepository;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl {

    private final FeedRepository feedRepository;
    private final FeedCategoryRepository feedCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final FeedImageRepository feedImageRepository;
    private final FeedImageCustomRepository feedImageCustomRepository;
    private final PopularFeedRepository popularFeedRepository;

    private static final String DEFAULT_CATEGORY = "all";

    @Transactional(readOnly = true)
    public Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }

    @Transactional(readOnly = true)
    public Slice<Feed> findFeedsByCategoryLabel(String category, Long lastFeedId, Long userId, PageRequest pageRequest,
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
                        lastFeedId, userId, pageRequest, genres);
            }
        }
    }

    // 이 부분 private으로 두는 게 맞을지 아니면 public으로 두는 게 나을지 고민
    private Category findCategoryByName(String categoryName) {
        return categoryRepository.findByCategoryName(CategoryName.valueOf(categoryName)).orElseThrow(
                () -> new CustomCategoryException(INVALID_CATEGORY_FORMAT,
                        "Category for the given feed was not found"));
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
