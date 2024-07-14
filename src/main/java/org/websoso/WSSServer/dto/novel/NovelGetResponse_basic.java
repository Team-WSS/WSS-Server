package org.websoso.WSSServer.dto.novel;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovel;

public record NovelGetResponse_basic(
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
    public static NovelGetResponse_basic of(Novel novel, UserNovel userNovel, String novelGenres,
                                            String novelGenreImage,
                                            Integer interestCount, Float novelRating, Integer novelRatingCount,
                                            Integer feedCount) {
        if (userNovel == null) {
            return new NovelGetResponse_basic(
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
        return new NovelGetResponse_basic(
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
                userNovel.getStatus().name(),
                userNovel.getStartDate() != null ? userNovel.getStartDate()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.forLanguageTag("ko")))
                        : null,
                userNovel.getEndDate() != null ? userNovel.getEndDate()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.forLanguageTag("ko")))
                        : null,
                userNovel.getIsInterest()
        );
    }
}
