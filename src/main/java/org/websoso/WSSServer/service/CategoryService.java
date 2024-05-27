package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.CategoryName.BL;
import static org.websoso.WSSServer.domain.common.CategoryName.DR;
import static org.websoso.WSSServer.domain.common.CategoryName.ETC;
import static org.websoso.WSSServer.domain.common.CategoryName.FA;
import static org.websoso.WSSServer.domain.common.CategoryName.LN;
import static org.websoso.WSSServer.domain.common.CategoryName.MF;
import static org.websoso.WSSServer.domain.common.CategoryName.MY;
import static org.websoso.WSSServer.domain.common.CategoryName.RF;
import static org.websoso.WSSServer.domain.common.CategoryName.RO;
import static org.websoso.WSSServer.domain.common.CategoryName.WU;
import static org.websoso.WSSServer.domain.common.Flag.N;
import static org.websoso.WSSServer.domain.common.Flag.Y;
import static org.websoso.WSSServer.exception.category.CategoryErrorCode.CATEGORY_NOT_FOUND;
import static org.websoso.WSSServer.exception.category.CategoryErrorCode.INVALID_CATEGORY_FORMAT;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Category;
import org.websoso.WSSServer.domain.Category.CategoryBuilder;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.common.CategoryName;
import org.websoso.WSSServer.domain.common.Flag;
import org.websoso.WSSServer.exception.category.exception.InvalidCategoryException;
import org.websoso.WSSServer.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public void createCategory(Feed feed, List<String> relevantCategories) {
        CategoryBuilder builder = Category.builder()
                .feed(feed);

        Category category = setCategory(builder, relevantCategories);

        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(Feed feed, List<String> relevantCategories) {
        Long categoryId = categoryRepository.findByFeed(feed).orElseThrow(() ->
                        new InvalidCategoryException(CATEGORY_NOT_FOUND, "Category for the given feed was not found"))
                .getCategoryId();

        CategoryBuilder builder = Category.builder()
                .categoryId(categoryId)
                .feed(feed);

        Category category = setCategory(builder, relevantCategories);

        categoryRepository.save(category);
    }

    private Category setCategory(CategoryBuilder builder, List<String> relevantCategories) {
        validateCategory(relevantCategories);

        return builder
                .isRf(getCategoryFlag(relevantCategories, RF))
                .isRo(getCategoryFlag(relevantCategories, RO))
                .isFa(getCategoryFlag(relevantCategories, FA))
                .isMf(getCategoryFlag(relevantCategories, MF))
                .isDr(getCategoryFlag(relevantCategories, DR))
                .isLn(getCategoryFlag(relevantCategories, LN))
                .isWu(getCategoryFlag(relevantCategories, WU))
                .isMy(getCategoryFlag(relevantCategories, MY))
                .isBl(getCategoryFlag(relevantCategories, BL))
                .isEtc(getCategoryFlag(relevantCategories, ETC))
                .build();
    }

    private void validateCategory(List<String> relevantCategories) {
        List<String> categoryNames = Arrays.stream(CategoryName.values()).map(CategoryName::getValue).toList();

        if (!categoryNames.containsAll(relevantCategories)) {
            throw new InvalidCategoryException(INVALID_CATEGORY_FORMAT, "invalid category format");
        }
    }

    private Flag getCategoryFlag(List<String> relevantCategories, CategoryName categoryName) {
        return relevantCategories.contains(categoryName.getValue()) ? Y : N;
    }

}
