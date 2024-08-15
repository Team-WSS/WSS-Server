package org.websoso.WSSServer.dto.userNovel;

import java.util.List;

public record UserTasteAttractivePointPreferencesAndKeywordsGetResponse(
        List<String> attractivePoints,
        List<TasteKeywordGetResponse> keywords
) {
}
