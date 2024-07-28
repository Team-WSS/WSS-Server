package org.websoso.WSSServer.dto.popularFeed;

public record PopularFeedGetResponse(
        Long feedId,
        String feedContent,
        Integer likeCount,
        Integer commentCount,
        Boolean isSpoiler
) {
}
