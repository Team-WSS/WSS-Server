package org.websoso.WSSServer.dto.userNovel;

import java.util.Map.Entry;

public record TasteKeywordGetResponse(
        String keywordName,
        Long keywordCount
) {

    public static TasteKeywordGetResponse of(Entry<String, Long> entry) {
        return new TasteKeywordGetResponse(
                entry.getKey(),
                entry.getValue()
        );
    }
}
