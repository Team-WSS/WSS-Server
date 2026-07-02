package org.websoso.WSSServer.novel.domain;

import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;

import java.math.BigDecimal;
import org.websoso.WSSServer.library.domain.UserNovel;

public record NovelStatisticsContribution(
        BigDecimal ratingSum,
        long ratingCount,
        long popularity
) {

    public static final NovelStatisticsContribution EMPTY =
            new NovelStatisticsContribution(BigDecimal.ZERO, 0L, 0L);

    public static NovelStatisticsContribution from(UserNovel userNovel) {
        float rating = userNovel.getUserNovelRating();
        boolean hasRating = Float.compare(rating, UserNovel.DEFAULT_RATING) != 0;
        boolean isPopular = Boolean.TRUE.equals(userNovel.getIsInterest())
                || (userNovel.getStatus() != null && userNovel.getStatus() != QUIT);

        return new NovelStatisticsContribution(
                hasRating ? new BigDecimal(Float.toString(rating)) : BigDecimal.ZERO,
                hasRating ? 1L : 0L,
                isPopular ? 1L : 0L
        );
    }
}
