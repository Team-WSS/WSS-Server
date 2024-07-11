package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomCategoryError.INVALID_CATEGORY_FORMAT;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.Category;
import org.websoso.WSSServer.domain.common.CategoryName;
import org.websoso.WSSServer.exception.exception.CustomCategoryException;
import org.websoso.WSSServer.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category getCategory(String label) {
        return categoryRepository.findByCategoryName(CategoryName.of(label).name())
                .orElseThrow(() -> new CustomCategoryException(INVALID_CATEGORY_FORMAT,
                        "Category for the given feed was not found"));
    }
}
