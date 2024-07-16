package org.websoso.WSSServer.dto.keyword;

import java.util.List;
import org.websoso.WSSServer.domain.common.KeywordCategoryName;

public record CategoryGetResponse(
        String categoryName,
        List<KeywordGetResponse> keywords
) {
    public static CategoryGetResponse of(KeywordCategoryName keywordCategoryName, List<KeywordGetResponse> keywords) {
        return new CategoryGetResponse(
                keywordCategoryName.getLabel(),
                keywords
        );
    }
}
