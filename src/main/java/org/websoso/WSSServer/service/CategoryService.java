package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Flag.Y;
import static org.websoso.WSSServer.exception.category.CategoryErrorCode.INVALID_CATEGORY_FORMAT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Category;
import org.websoso.WSSServer.domain.Category.CategoryBuilder;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.exception.category.exeption.InvalidCategoryException;
import org.websoso.WSSServer.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public void createCategory(Feed feed, List<String> relevantCategories) {
        Category.CategoryBuilder builder = Category.builder()
                .feed(feed);

        for (String category : relevantCategories) {
            setCategory(builder, category);
        }

        Category category = builder.build();
        categoryRepository.save(category);
    }

    private void setCategory(CategoryBuilder builder, String category) {
        switch (category) {
            case "romanceFantasy" -> builder.isRf(Y);
            case "romance" -> builder.isRo(Y);
            case "fantasy" -> builder.isFa(Y);
            case "modernFantasy" -> builder.isMf(Y);
            case "drama" -> builder.isDr(Y);
            case "lightNovel" -> builder.isLn(Y);
            case "wuxia" -> builder.isWu(Y);
            case "mystery" -> builder.isMy(Y);
            case "BL" -> builder.isBl(Y);
            default -> throw new InvalidCategoryException(INVALID_CATEGORY_FORMAT, "invalid category format");
        }
    }
}