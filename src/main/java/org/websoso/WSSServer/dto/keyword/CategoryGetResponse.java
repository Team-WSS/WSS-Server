package org.websoso.WSSServer.dto.keyword;

import java.util.List;
import org.websoso.WSSServer.domain.common.KeywordCategory;

public record CategoryGetResponse(
        String categoryName,
        List<KeywordGetResponse> keywords
) {
    public static CategoryGetResponse of(KeywordCategory keywordCategory, List<KeywordGetResponse> keywords) {
        return new CategoryGetResponse(
                keywordCategory.getName(),
                keywords
        );
    }
}
