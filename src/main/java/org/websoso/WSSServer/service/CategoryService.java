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
import static org.websoso.WSSServer.exception.error.CustomCategoryError.CATEGORY_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomCategoryError.INVALID_CATEGORY_FORMAT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Category;
import org.websoso.WSSServer.domain.Category.CategoryBuilder;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.common.CategoryName;
import org.websoso.WSSServer.exception.exception.CustomCategoryException;
import org.websoso.WSSServer.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public void createCategory(Feed feed, List<String> relevantCategories) {
        CategoryBuilder builder = Category.builder()
                .feed(feed);

        Category category = setCategory(builder, relevantCategories);

        categoryRepository.save(category);
    }

    public void updateCategory(Feed feed, List<String> relevantCategories) {
        Long categoryId = categoryRepository.findByFeed(feed).orElseThrow(() ->
                        new CustomCategoryException(CATEGORY_NOT_FOUND, "Category for the given feed was not found"))
                .getCategoryId();

        CategoryBuilder builder = Category.builder()
                .categoryId(categoryId)
                .feed(feed);

        Category category = setCategory(builder, relevantCategories);

        categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<String> getRelevantCategoryNames(Category category) {
        List<String> relevantCategories = new ArrayList<>();

        if (category.getIsRf()) {
            relevantCategories.add(RF.getValue());
        }
        if (category.getIsRo()) {
            relevantCategories.add(RO.getValue());
        }
        if (category.getIsFa()) {
            relevantCategories.add(FA.getValue());
        }
        if (category.getIsMf()) {
            relevantCategories.add(MF.getValue());
        }
        if (category.getIsDr()) {
            relevantCategories.add(DR.getValue());
        }
        if (category.getIsLn()) {
            relevantCategories.add(LN.getValue());
        }
        if (category.getIsWu()) {
            relevantCategories.add(WU.getValue());
        }
        if (category.getIsMy()) {
            relevantCategories.add(MY.getValue());
        }
        if (category.getIsBl()) {
            relevantCategories.add(BL.getValue());
        }
        if (category.getIsEtc()) {
            relevantCategories.add(ETC.getValue());
        }

        return relevantCategories;
    }

    private Category setCategory(CategoryBuilder builder, List<String> relevantCategories) {
        validateCategory(relevantCategories);

        return builder
                .isRf(isContainCategoryName(relevantCategories, RF))
                .isRo(isContainCategoryName(relevantCategories, RO))
                .isFa(isContainCategoryName(relevantCategories, FA))
                .isMf(isContainCategoryName(relevantCategories, MF))
                .isDr(isContainCategoryName(relevantCategories, DR))
                .isLn(isContainCategoryName(relevantCategories, LN))
                .isWu(isContainCategoryName(relevantCategories, WU))
                .isMy(isContainCategoryName(relevantCategories, MY))
                .isBl(isContainCategoryName(relevantCategories, BL))
                .isEtc(isContainCategoryName(relevantCategories, ETC))
                .build();
    }

    private void validateCategory(List<String> relevantCategories) {
        List<String> categoryNames = Arrays.stream(CategoryName.values()).map(CategoryName::getValue).toList();

        if (!categoryNames.containsAll(relevantCategories)) {
            throw new CustomCategoryException(INVALID_CATEGORY_FORMAT, "invalid category format");
        }
    }

    private Boolean isContainCategoryName(List<String> relevantCategories, CategoryName categoryName) {
        return relevantCategories.contains(categoryName.getValue());
    }

}
