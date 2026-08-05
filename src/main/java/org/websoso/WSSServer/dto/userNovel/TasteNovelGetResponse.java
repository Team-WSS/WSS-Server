package org.websoso.WSSServer.dto.userNovel;

import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelStatistics;

public record TasteNovelGetResponse(
        Long novelId,
        String title,
        String author,
        String novelImage,
        Long interestCount,
        Float novelRating,
        Long novelRatingCount
) {

    // 작품과 미리 집계한 관심 수로 취향 추천 응답을 생성한다.
    public static TasteNovelGetResponse of(Novel tasteNovel, Long interestCount) {
        NovelStatistics statistics = tasteNovel.getNovelStatistics();
        Float novelRating = statistics == null
                ? 0.0f
                : statistics.getAverageRating().floatValue();
        Long novelRatingCount = statistics == null
                ? 0L
                : statistics.getRatingCount();

        return new TasteNovelGetResponse(
                tasteNovel.getNovelId(),
                tasteNovel.getTitle(),
                tasteNovel.getAuthor(),
                tasteNovel.getNovelImage(),
                interestCount,
                novelRating,
                novelRatingCount
        );
    }
}
