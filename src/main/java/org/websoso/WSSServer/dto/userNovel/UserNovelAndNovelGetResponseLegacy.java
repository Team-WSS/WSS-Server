package org.websoso.WSSServer.dto.userNovel;

import org.websoso.WSSServer.domain.UserNovel;

public record UserNovelAndNovelGetResponseLegacy(
        Long userNovelId,
        Long novelId,
        String author,
        String novelImage,
        String title,
        Float novelRating
) {
    public static UserNovelAndNovelGetResponseLegacy of(UserNovel userNovel) {
        return new UserNovelAndNovelGetResponseLegacy(
                userNovel.getUserNovelId(),
                userNovel.getNovel().getNovelId(),
                userNovel.getNovel().getAuthor(),
                userNovel.getNovel().getNovelImage(),
                userNovel.getNovel().getTitle(),
                userNovel.getUserNovelRating()
        );
    }
}
