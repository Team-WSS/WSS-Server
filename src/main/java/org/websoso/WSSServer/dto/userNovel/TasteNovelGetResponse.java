package org.websoso.WSSServer.dto.userNovel;

import org.websoso.WSSServer.domain.Novel;

public record TasteNovelGetResponse(
        Long novelId,
        String title,
        String author,
        String novelImage
//        Integer interestCount,
//        Float novelRating,
//        Integer novelRatingCount
) {

    public static TasteNovelGetResponse of(Novel tasteNovel) {
        return new TasteNovelGetResponse(
                tasteNovel.getNovelId(),
                tasteNovel.getTitle(),
                tasteNovel.getAuthor(),
                tasteNovel.getNovelImage()
        );
    }
}
