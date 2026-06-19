package org.websoso.WSSServer.feed.feed.controller.dto;

public record UserFeedGetResponse(
        Long feedId,
        String feedContent,
        String createdDate,
        Boolean isSpoiler,
        Boolean isModified,
        Boolean isLiked,
        Integer likeCount,
        Integer commentCount,
        Long novelId,
        String title,
        Float novelRating,
        Long novelRatingCount,
        Boolean isPublic,
        String genre,
        Float userNovelRating,
        String thumbnailUrl,
        Integer imageCount,
        Float feedWriterNovelRating
) {

}
