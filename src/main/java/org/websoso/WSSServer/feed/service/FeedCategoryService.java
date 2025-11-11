package org.websoso.WSSServer.feed.service;

import static org.websoso.WSSServer.exception.error.CustomCategoryError.CATEGORY_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomCategoryError.INVALID_CATEGORY_FORMAT;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.common.CategoryName;
import org.websoso.WSSServer.exception.exception.CustomCategoryException;
import org.websoso.WSSServer.feed.domain.Category;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.FeedCategory;
import org.websoso.WSSServer.feed.repository.CategoryRepository;
import org.websoso.WSSServer.feed.repository.FeedCategoryRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedCategoryService {

    private final FeedCategoryRepository feedcategoryRepository;
    private final CategoryRepository categoryRepository;

    public void createFeedCategory(Feed feed, List<String> relevantCategories) {
        for (String relevantCategory : relevantCategories) {
            Category category = findCategoryByName(relevantCategory);
            feedcategoryRepository.save(FeedCategory.create(feed, category));
        }
    }

    public void updateFeedCategory(Feed feed, List<String> relevantCategories) {
        List<FeedCategory> feedCategories = feedcategoryRepository.findByFeed(feed);

        if (feedCategories.isEmpty()) {
            throw new CustomCategoryException(CATEGORY_NOT_FOUND, "Category for the given feed was not found");
        }

        Set<Category> categories = feedCategories.stream().map(FeedCategory::getCategory).collect(Collectors.toSet());

        Set<Category> newCategories = relevantCategories.stream().map(this::findCategoryByName)
                .collect(Collectors.toSet());

        for (Category newCategory : newCategories) {
            if (categories.contains(newCategory)) {
                categories.remove(newCategory);
            } else {
                feedcategoryRepository.save(FeedCategory.create(feed, newCategory));
            }
        }

        for (Category category : categories) {
            feedcategoryRepository.deleteByCategoryAndFeed(category, feed);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getRelevantCategoryNames(List<FeedCategory> feedCategories) {
        return feedCategories.stream().map(feedCategory -> feedCategory.getCategory().getCategoryName().getLabel())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Slice<Feed> getFeedsByCategoryLabel(String category, Long lastFeedId, Long userId, PageRequest pageRequest) {
        return feedcategoryRepository.findFeedsByCategory(findCategoryByName(category), lastFeedId, userId,
                pageRequest);
    }

    @Transactional(readOnly = true)
    public Slice<Feed> getRecommendedFeedsByCategoryLabel(String category, Long lastFeedId, Long userId,
                                                          PageRequest pageRequest, List<Genre> genres) {
        return feedcategoryRepository.findRecommendedFeedsByCategoryLabel(findCategoryByName(category), lastFeedId,
                userId, pageRequest, genres);
    }

    private Category findCategoryByName(String categoryName) {
        return categoryRepository.findByCategoryName(CategoryName.valueOf(categoryName)).orElseThrow(
                () -> new CustomCategoryException(INVALID_CATEGORY_FORMAT,
                        "Category for the given feed was not found"));
    }

}
