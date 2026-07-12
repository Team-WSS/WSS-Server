package org.websoso.WSSServer.feed.feed.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.websoso.WSSServer.feed.feed.controller.dto.FeedInfo;
import org.websoso.WSSServer.util.TimeFormatUtil;

public record FeedInfoRow(
        Long feedId,
        Long userId,
        String nickname,
        String avatarImage,
        LocalDateTime createdDate,
        String feedContent,
        Long likeCount,
        Boolean isLiked,
        Long commentCount,
        Long novelId,
        String title,
        Long novelRatingCount,
        BigDecimal novelRating,
        Boolean isSpoiler,
        Boolean isModified,
        Boolean isMyFeed,
        Boolean isPublic,
        String thumbnailUrl,
        Long imageCount,
        String genreName,
        Float userNovelRating,
        Float feedWriterNovelRating
) {

    public FeedInfo toResponse() {
        return new FeedInfo(
                feedId,
                userId,
                nickname,
                avatarImage,
                TimeFormatUtil.formatRelativeDateTime(createdDate),
                feedContent,
                toInteger(likeCount),
                isLiked,
                toInteger(commentCount),
                novelId,
                title,
                toInteger(novelRatingCount),
                roundToFirstDecimal(novelRating),
                isSpoiler,
                isModified,
                isMyFeed,
                isPublic,
                thumbnailUrl,
                toInteger(imageCount),
                genreName,
                userNovelRating,
                feedWriterNovelRating
        );
    }

    private static Integer toInteger(Long value) {
        return value == null ? 0 : value.intValue();
    }

    private static Float roundToFirstDecimal(BigDecimal value) {
        if (value == null) {
            return null;
        }

        return Math.round(value.floatValue() * 10) / 10.0f;
    }
}
