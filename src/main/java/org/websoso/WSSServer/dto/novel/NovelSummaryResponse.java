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
    public static NovelSummaryResponse of(Novel novel, long interestCount) {
        float novelRating = Math.round(novel.getAverageRating().floatValue() * 10.0f) / 10.0f;

        return new NovelSummaryResponse(
                novel.getNovelId(),
                novel.getNovelImage(),
                novel.getTitle(),
                novel.getAuthor(),
                interestCount,
                novelRating,
                novel.getRatingCount()
        );
    }
}
