package org.websoso.WSSServer.dto.keyword;

import org.websoso.WSSServer.domain.Keyword;

public record KeywordGetResponse(
        Integer keywordId,
        String keywordName
) {
    public static KeywordGetResponse of(Keyword keyword) {
        return new KeywordGetResponse(
                keyword.getKeywordId(),
                keyword.getKeywordName()
        );
    }
}
