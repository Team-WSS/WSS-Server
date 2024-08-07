package org.websoso.WSSServer.dto.userNovel;

import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovel;

public record TasteNovelGetResponse(
        Long novelId,
        String title,
        String author,
        String novelImage,
        Long interestCount
//        Float novelRating,
//        Integer novelRatingCount
) {

    public static TasteNovelGetResponse of(Novel tasteNovel) {
        return new TasteNovelGetResponse(
                tasteNovel.getNovelId(),
                tasteNovel.getTitle(),
                tasteNovel.getAuthor(),
                tasteNovel.getNovelImage(),
                getInterestCount(tasteNovel)
        );
    }

    private static Long getInterestCount(Novel tasteNovel) {
        return tasteNovel.getUserNovels()
                .stream()
                .map(UserNovel::getIsInterest)
                .count();
    }

    private static Long getNovelRatingCount(Novel tasteNovel) {
        return tasteNovel.getUserNovels()
                .stream()
                .filter(userNovel -> userNovel.getUserNovelRating() != 0.0f)
                .count();
    }

    private static Float getNovelRatingSum(Novel tasteNovel) {
        return (float) tasteNovel.getUserNovels()
                .stream()
                .filter(userNovel -> userNovel.getUserNovelRating() != 0.0f)
                .mapToDouble(UserNovel::getUserNovelRating)
                .sum();
    }

    private static float getNovelRating(Novel tasteNovel, Long novelRatingCount) {
        return novelRatingCount > 0
                ? getNovelRatingSum(tasteNovel) / novelRatingCount
                : 0.0f;
    }
}
