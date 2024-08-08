package org.websoso.WSSServer.dto.userNovel;

import java.util.List;

public record TasteNovelsGetResponse(
        List<TasteNovelGetResponse> tasteNovels
) {

    public static TasteNovelsGetResponse of(List<TasteNovelGetResponse> tasteNovelGetResponses) {
        return new TasteNovelsGetResponse(tasteNovelGetResponses);
    }
}
