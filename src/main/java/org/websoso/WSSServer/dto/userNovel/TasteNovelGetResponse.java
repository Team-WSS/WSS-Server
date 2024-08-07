package org.websoso.WSSServer.dto.userNovel;

public record TasteNovelGetResponse(
        Long novelId,
        String title,
        String author,
        String novelImage,
        Integer interestCount,
        Float novelRating,
        Integer novelRatingCount
) {
}
