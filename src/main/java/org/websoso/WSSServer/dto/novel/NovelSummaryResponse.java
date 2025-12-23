package org.websoso.WSSServer.dto.novel;

import org.websoso.WSSServer.novel.domain.Novel;

public record NovelSummaryResponse(
        long novelId,
        String novelImage,
        String title,
        String author,
        long interestCount,
        float novelRating,
        long novelRatingCount
) {
    public static NovelSummaryResponse of(Novel novel, long interestCount, float novelRating,
                                             long novelRatingCount) {
        return new NovelSummaryResponse(
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
