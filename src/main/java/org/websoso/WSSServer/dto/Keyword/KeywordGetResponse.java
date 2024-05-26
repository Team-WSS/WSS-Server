package org.websoso.WSSServer.dto.Keyword;

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
