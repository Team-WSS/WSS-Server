package org.websoso.WSSServer.dto.Keyword;

import lombok.Getter;
import org.websoso.WSSServer.domain.Keyword;

@Getter
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
