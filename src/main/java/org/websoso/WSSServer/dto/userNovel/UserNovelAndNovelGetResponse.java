package org.websoso.WSSServer.dto.userNovel;

import org.websoso.WSSServer.domain.UserNovel;

public record UserNovelAndNovelGetResponse(
        Long userNovelId,
        Long novelId,
        String author,
        String novelImage,
        String title,
        Float novelRating
) {

    public static UserNovelAndNovelGetResponse of(UserNovel userNovel) {
        return new UserNovelAndNovelGetResponse(
                userNovel.getUserNovelId(),
                userNovel.getNovel().getNovelId(),
                userNovel.getNovel().getAuthor(),
                userNovel.getNovel().getNovelImage(),
                userNovel.getNovel().getTitle(),
                userNovel.getUserNovelRating()
        );
    }
}
