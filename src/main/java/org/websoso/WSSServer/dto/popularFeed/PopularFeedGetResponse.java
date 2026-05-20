package org.websoso.WSSServer.dto.popularFeed;

import org.websoso.WSSServer.feed.domain.PopularFeed;

public record PopularFeedGetResponse(
        Long feedId,
        String feedContent,
        Integer likeCount,
        Integer commentCount,
        Boolean isSpoiler,
        Boolean isPublic,
        String novelImage,
        String novelGenreImage
) {

    public static PopularFeedGetResponse of(PopularFeed popularFeed, String novelImage, String novelGenreImage) {
        return new PopularFeedGetResponse(
                popularFeed.getFeed().getFeedId(),
                popularFeed.getFeed().getFeedContent(),
                popularFeed.getFeed().getLikes().size(),
                popularFeed.getFeed().getComments().size(),
                popularFeed.getFeed().getIsSpoiler(),
                popularFeed.getFeed().getIsPublic(),
                novelImage,
                novelGenreImage
        );
    }
}
