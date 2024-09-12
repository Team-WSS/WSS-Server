package org.websoso.WSSServer.dto.novel;

import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovel;

public record NovelGetResponseBasic(
        Long userNovelId,
        String novelTitle,
        String novelImage,
        String novelGenres,
        String novelGenreImage,
        Boolean isNovelCompleted,
        String author,
        Integer interestCount,
        Float novelRating,
        Integer novelRatingCount,
        Integer feedCount,
        Float userNovelRating,
        String readStatus,
        String startDate,
        String endDate,
        Boolean isUserNovelInterest
) {
    public static NovelGetResponseBasic of(Novel novel, UserNovel userNovel, String novelGenres,
                                           String novelGenreImage,
                                           Integer interestCount, Float novelRating, Integer novelRatingCount,
                                           Integer feedCount) {
        if (userNovel == null) {
            return new NovelGetResponseBasic(
                    null,
                    novel.getTitle(),
                    novel.getNovelImage(),
                    novelGenres,
                    novelGenreImage,
                    novel.getIsCompleted(),
                    novel.getAuthor(),
                    interestCount,
                    novelRating,
                    novelRatingCount,
                    feedCount,
                    0.0f,
                    null,
                    null,
                    null,
                    false
            );
        }
        return new NovelGetResponseBasic(
                userNovel.getUserNovelId(),
                novel.getTitle(),
                novel.getNovelImage(),
                novelGenres,
                novelGenreImage,
                novel.getIsCompleted(),
                novel.getAuthor(),
                interestCount,
                novelRating,
                novelRatingCount,
                feedCount,
                userNovel.getUserNovelRating(),
                userNovel.getStatus() == null ? null : userNovel.getStatus().name(),
                userNovel.getStartDate() != null ? userNovel.getStartDate().toString() : null,
                userNovel.getEndDate() != null ? userNovel.getEndDate().toString() : null,
                userNovel.getIsInterest()
        );
    }
}
