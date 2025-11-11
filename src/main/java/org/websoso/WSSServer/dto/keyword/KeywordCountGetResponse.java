package org.websoso.WSSServer.dto.keyword;

import org.websoso.WSSServer.library.domain.Keyword;

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
