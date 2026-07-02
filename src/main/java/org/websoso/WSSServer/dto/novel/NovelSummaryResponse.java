package org.websoso.WSSServer.dto.novel;

import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelStatistics;

public record NovelSummaryResponse(
        long novelId,
        String novelImage,
        String title,
        String author,
        long interestCount,
        float novelRating,
        long novelRatingCount
) {
    /**
     * 작품과 연결된 통계로 검색 결과 요약 응답을 생성한다.
     */
    public static NovelSummaryResponse of(Novel novel, long interestCount) {
        NovelStatistics statistics = novel.getNovelStatistics();
        float novelRating = statistics == null
                ? 0.0f
                : Math.round(statistics.getAverageRating().floatValue() * 10.0f) / 10.0f;
        long novelRatingCount = statistics == null
                ? 0L
                : statistics.getRatingCount();

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
