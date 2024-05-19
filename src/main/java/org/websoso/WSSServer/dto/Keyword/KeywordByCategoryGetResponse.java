package org.websoso.WSSServer.dto.Keyword;

import java.util.List;

public record KeywordByCategoryGetResponse(
        List<CategoryGetResponse> categorys
) {
    public static KeywordByCategoryGetResponse of(List<CategoryGetResponse> categorys) {
        return new KeywordByCategoryGetResponse(categorys);
    }
}
