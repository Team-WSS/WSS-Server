package org.websoso.WSSServer.feed.repository;

import org.websoso.WSSServer.dto.popularFeed.PopularFeedGetResponse;

public record PopularFeedInfoRow(
        Long feedId,
        String feedContent,
        Long likeCount,
        Long commentCount,
        Boolean isSpoiler,
        Boolean isPublic,
        String novelTitle,
        String novelImage,
        String novelGenre
) {

    public PopularFeedGetResponse toResponse() {
        return new PopularFeedGetResponse(
                feedId,
                feedContent,
                toInteger(likeCount),
                toInteger(commentCount),
                isSpoiler,
                isPublic,
                novelTitle,
                novelImage,
                novelGenre
        );
    }

    private static Integer toInteger(Long value) {
        return value == null ? 0 : value.intValue();
    }
}
