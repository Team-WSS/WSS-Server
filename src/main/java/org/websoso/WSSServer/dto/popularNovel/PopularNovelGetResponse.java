package org.websoso.WSSServer.dto.popularNovel;

import org.websoso.WSSServer.domain.Novel;

public record PopularNovelGetResponse(
        Long novelId,
        String title,
        String novelImage
) {

    public static PopularNovelGetResponse of(Novel novel) {
        return new PopularNovelGetResponse(
                novel.getNovelId(),
                novel.getTitle(),
                novel.getNovelImage()
        );
    }
}
