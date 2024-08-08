package org.websoso.WSSServer.dto.userNovel;

public record UserNovelAndNovelGetResponse(
        Long userNovelId,
        Long novelId,
        String author,
        String novelImage,
        String title,
        Float novelRating
) {
}
