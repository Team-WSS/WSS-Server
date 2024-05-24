package org.websoso.WSSServer.dto.Keyword;

import java.util.List;

public record KeywordByCategoryGetResponse(
        List<CategoryGetResponse> categories
) {
    public static KeywordByCategoryGetResponse of(List<CategoryGetResponse> categories) {
        return new KeywordByCategoryGetResponse(categories);
    }
}
