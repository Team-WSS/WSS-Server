package org.websoso.WSSServer.dto.novel;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.common.Flag;

public record NovelGetResponse(
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
    public static NovelGetResponse of(Novel novel, UserNovel userNovel, NovelStatistics novelStatistics,
                                      String novelGenres, String novelGenreImage) {
        return new NovelGetResponse(
                novel.getNovelId(),
                novel.getTitle(),
                novel.getNovelImage(),
                novelGenres,
                novelGenreImage,
                novel.getIsCompleted().equals(Flag.Y),
                novel.getAuthor(),
                novelStatistics.getInterestCount(),
                novel.getNovelRatingSum() / novel.getNovelRatingCount(),
                novel.getNovelRatingCount(),
                novelStatistics.getNovelFeedCount(),
                userNovel.getUserNovelRating(),
                userNovel.getStatus().getName(),
                userNovel.getStartDate()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.forLanguageTag("ko"))),
                userNovel.getEndDate().toString(),
                userNovel.getIsInterest().equals(Flag.Y)
        );
    }
}
