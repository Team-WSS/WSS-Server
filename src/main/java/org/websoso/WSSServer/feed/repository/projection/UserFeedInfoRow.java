package org.websoso.WSSServer.feed.repository.projection;

import java.time.LocalDateTime;
import org.websoso.WSSServer.feed.controller.dto.UserFeedGetResponse;
import org.websoso.WSSServer.util.TimeFormatUtil;

public record UserFeedInfoRow(
        Long feedId,
        String feedContent,
        LocalDateTime createdDate,
        Boolean isSpoiler,
        Boolean isModified,
        Boolean isLiked,
        Long likeCount,
        Long commentCount,
        Long novelId,
        String title,
        Double novelRating,
        Long novelRatingCount,
        Boolean isPublic,
        String genre,
        Float userNovelRating,
        String thumbnailUrl,
        Long imageCount,
        Float feedWriterNovelRating
) {

    public UserFeedGetResponse toResponse() {
        return new UserFeedGetResponse(
                feedId,
                feedContent,
                TimeFormatUtil.formatRelativeDateTime(createdDate),
                isSpoiler,
                isModified,
                isLiked,
                toInteger(likeCount),
                toInteger(commentCount),
                novelId,
                title,
                roundToFirstDecimal(novelRating),
                novelRatingCount,
                isPublic,
                genre,
                userNovelRating,
                thumbnailUrl,
                toInteger(imageCount),
                feedWriterNovelRating
        );
    }

    private static Integer toInteger(Long value) {
        return value == null ? 0 : value.intValue();
    }

    private static Float roundToFirstDecimal(Double value) {
        if (value == null) {
            return null;
        }

        return Math.round(value.floatValue() * 10) / 10.0f;
    }
}
