package org.websoso.WSSServer.dto.novel;

import static org.websoso.WSSServer.domain.common.Flag.Y;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.domain.UserNovel;

// TODO 이름 변경(작품 정보 조회 뷰에서 상단, 기본정보를 제공하는 부분)
public record NovelGetResponse1(
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
    public static NovelGetResponse1 of(Novel novel, UserNovel userNovel, NovelStatistics novelStatistics,
                                       String novelGenres, String novelGenreImage) {
        Float novelRating = novel.getNovelRatingCount() > 0 ?
                Math.round((novel.getNovelRatingSum() / novel.getNovelRatingCount()) * 10) / 10.0f : 0;
        if (userNovel == null) {
            return new NovelGetResponse1(
                    null,
                    novel.getTitle(),
                    novel.getNovelImage(),
                    novelGenres,
                    novelGenreImage,
                    novel.getIsCompleted().equals(Y),
                    novel.getAuthor(),
                    novelStatistics.getInterestCount(),
                    novelRating,
                    novel.getNovelRatingCount(),
                    novelStatistics.getNovelFeedCount(),
                    0f,
                    null,
                    null,
                    null,
                    false
            );
        }
        return new NovelGetResponse1(
                userNovel.getUserNovelId(),
                novel.getTitle(),
                novel.getNovelImage(),
                novelGenres,
                novelGenreImage,
                novel.getIsCompleted().equals(Y),
                novel.getAuthor(),
                novelStatistics.getInterestCount(),
                novelRating,
                novel.getNovelRatingCount(),
                novelStatistics.getNovelFeedCount(),
                userNovel.getUserNovelRating(),
                userNovel.getStatus().getName(),
                userNovel.getStartDate() != null ? userNovel.getStartDate()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.forLanguageTag("ko")))
                        : null,
                userNovel.getEndDate() != null ? userNovel.getEndDate()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.forLanguageTag("ko")))
                        : null,
                userNovel.getIsInterest().equals(Y)
        );
    }
}
