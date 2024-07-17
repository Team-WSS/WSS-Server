package org.websoso.WSSServer.dto.keyword;

import java.util.List;
import org.websoso.WSSServer.domain.KeywordCategory;

public record CategoryGetResponse(
        String categoryName,
        String categoryImage,
        List<KeywordGetResponse> keywords
) {
    public static CategoryGetResponse of(KeywordCategory keywordCategory, List<KeywordGetResponse> keywords) {
        return new CategoryGetResponse(
                keywordCategory.getKeywordCategoryName(),
                keywordCategory.getKeywordCategoryImage(),
                keywords
        );
    }
}
