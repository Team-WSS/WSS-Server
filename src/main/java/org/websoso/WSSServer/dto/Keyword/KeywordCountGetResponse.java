package org.websoso.WSSServer.dto.Keyword;

import org.websoso.WSSServer.domain.Keyword;

public record KeywordCountGetResponse(
        String keywordName,
        Integer keywordCount
) {
    public static KeywordCountGetResponse of(Keyword keyword, Integer keywordCount) {
        return new KeywordCountGetResponse(
                keyword.getKeywordName(),
                keywordCount
        );
    }
}
