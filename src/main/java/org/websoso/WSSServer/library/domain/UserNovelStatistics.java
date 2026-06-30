package org.websoso.WSSServer.library.domain;

import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;

import java.math.BigDecimal;

public record UserNovelStatistics(
        BigDecimal ratingSum,
        long ratingCount,
        long popularity
) {

    public static final UserNovelStatistics EMPTY = new UserNovelStatistics(BigDecimal.ZERO, 0L, 0L);

    public static UserNovelStatistics from(UserNovel userNovel) {
        float rating = userNovel.getUserNovelRating();
        boolean hasRating = Float.compare(rating, UserNovel.DEFAULT_RATING) != 0;
        boolean isPopular = Boolean.TRUE.equals(userNovel.getIsInterest())
                || (userNovel.getStatus() != null && userNovel.getStatus() != QUIT);

        return new UserNovelStatistics(
                hasRating ? new BigDecimal(Float.toString(rating)) : BigDecimal.ZERO,
                hasRating ? 1L : 0L,
                isPopular ? 1L : 0L
        );
    }
}
