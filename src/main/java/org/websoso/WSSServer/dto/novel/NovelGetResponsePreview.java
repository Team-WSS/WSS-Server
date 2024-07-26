package org.websoso.WSSServer.dto.novel;

import org.websoso.WSSServer.domain.Novel;

public record NovelGetResponsePreview(
        Long novelId,
        String novelImage,
        String title,
        String author,
        Integer interestCount,
        Float novelRating,
        Integer novelRatingCount
) {
    public static NovelGetResponsePreview of(Novel novel, Integer interestCount, Float novelRating,
                                             Integer novelRatingCount) {
        return new NovelGetResponsePreview(
                novel.getNovelId(),
                novel.getNovelImage(),
                novel.getTitle(),
                novel.getAuthor(),
                interestCount,
                novelRating,
                novelRatingCount
        );
    }
}
