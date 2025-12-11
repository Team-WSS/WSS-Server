package org.websoso.WSSServer.dto.popularFeed;

import org.websoso.WSSServer.feed.domain.PopularFeed;

public record PopularFeedGetResponse(
        Long feedId,
        String feedContent,
        Integer likeCount,
        Integer commentCount,
        Boolean isSpoiler,
        Boolean isPublic
) {

    public static PopularFeedGetResponse of(PopularFeed popularFeed) {
        return new PopularFeedGetResponse(
                popularFeed.getFeed().getFeedId(),
                popularFeed.getFeed().getFeedContent(),
                popularFeed.getFeed().getLikes().size(),
                popularFeed.getFeed().getComments().size(),
                popularFeed.getFeed().getIsSpoiler(),
                popularFeed.getFeed().getIsPublic()
        );
    }
}
