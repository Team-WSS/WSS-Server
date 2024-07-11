package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomCategoryError.CATEGORY_NOT_FOUND;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Category;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.FeedCategory;
import org.websoso.WSSServer.exception.exception.CustomCategoryException;
import org.websoso.WSSServer.repository.FeedCategoryRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedCategoryService {

    private final FeedCategoryRepository feedcategoryRepository;
    private final Categoryservice categoryservice;

    public void createFeedCategory(Feed feed, List<String> relevantCategories) {
        for (String relevantCategory : relevantCategories) {
            feedcategoryRepository.save(FeedCategory.create(feed, categoryservice.getCategory(relevantCategory)));
        }
    }

    public void updateFeedCategory(Feed feed, List<String> relevantCategories) {
        Set<Category> categories = feedcategoryRepository.findByFeed(feed).orElseThrow(
                        () -> new CustomCategoryException(CATEGORY_NOT_FOUND, "Category for the given feed was not found"))
                .stream()
                .map(FeedCategory::getCategory).collect(Collectors.toSet());

        Set<Category> newCategories = relevantCategories.stream().map(categoryservice::getCategory)
                .collect(Collectors.toSet());

        for (Category newCategory : newCategories) {
            if (categories.contains(newCategory)) {
                categories.remove(newCategory);
            } else {
                feedcategoryRepository.save(FeedCategory.create(feed, newCategory));
            }
        }

        for (Category category : categories) {
            feedcategoryRepository.deleteByCategory(category);
        }
    }

    public void deleteFeedCategoryByFeed(Feed feed) {
        feedcategoryRepository.deleteAll(feed.getFeedCategories());
    }

    @Transactional(readOnly = true)
    public List<String> getRelevantCategoryNames(List<FeedCategory> feedCategories) {
        return feedCategories.stream()
                .map(feedCategory -> feedCategory.getCategory().getCategoryName().getLabel())
                .collect(Collectors.toList());
    }

}
