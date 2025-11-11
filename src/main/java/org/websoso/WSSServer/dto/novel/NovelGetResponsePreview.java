package org.websoso.WSSServer.dto.novel;

import org.websoso.WSSServer.novel.domain.Novel;

public record NovelGetResponsePreview(
        Long novelId,
        String novelImage,
        String title,
        String author,
        Long interestCount,
        Float novelRating,
        Long novelRatingCount
) {
    public static NovelGetResponsePreview of(Novel novel, Long interestCount, Float novelRating,
                                             Long novelRatingCount) {
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
