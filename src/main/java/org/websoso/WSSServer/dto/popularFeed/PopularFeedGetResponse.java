package org.websoso.WSSServer.dto.popularFeed;

import org.websoso.WSSServer.domain.PopularFeed;

public record PopularFeedGetResponse(
        Long feedId,
        String feedContent,
        Integer likeCount,
        Integer commentCount,
        Boolean isSpoiler
) {

    public static PopularFeedGetResponse of(PopularFeed popularFeed) {
        return new PopularFeedGetResponse(
                popularFeed.getFeed().getFeedId(),
                popularFeed.getFeed().getFeedContent(),
                popularFeed.getFeed().getLikes().size(),
                popularFeed.getFeed().getComments().size(),
                popularFeed.getFeed().getIsSpoiler()
        );
    }
}
