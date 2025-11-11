package org.websoso.WSSServer.dto.sosoPick;

import org.websoso.WSSServer.novel.domain.Novel;

public record SosoPickNovelGetResponse(
        Long novelId,
        String novelImage,
        String title
) {
    public static SosoPickNovelGetResponse of(Novel novel) {
        return new SosoPickNovelGetResponse(
                novel.getNovelId(),
                novel.getNovelImage(),
                novel.getTitle()
        );
    }
}
