package org.websoso.WSSServer.dto.keyword;

import org.websoso.WSSServer.library.domain.KeywordCategory;

import java.util.List;

public record KeywordPopularGetResponse(
        List<KeywordGetResponse> keywords
) {
    public static KeywordPopularGetResponse of(List<KeywordGetResponse> keywords) {
        return new KeywordPopularGetResponse(
                keywords
        );
    }
}
