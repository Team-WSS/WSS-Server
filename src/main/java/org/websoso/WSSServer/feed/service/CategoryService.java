package org.websoso.WSSServer.feed.service;

import static org.websoso.WSSServer.exception.error.CustomCategoryError.INVALID_CATEGORY_FORMAT;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.domain.Category;
import org.websoso.WSSServer.domain.common.CategoryName;
import org.websoso.WSSServer.exception.exception.CustomCategoryException;
import org.websoso.WSSServer.feed.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Category getCategory(String categoryName) {
        return categoryRepository.findByCategoryName(CategoryName.valueOf(categoryName))
                .orElseThrow(() -> new CustomCategoryException(INVALID_CATEGORY_FORMAT,
                        "Category for the given feed was not found"));
    }

}
