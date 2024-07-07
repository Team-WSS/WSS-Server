package org.websoso.WSSServer.dto.feed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.dto.user.UserBasicInfo;

public record FeedGetResponse(
        Long userId,
        String nickname,
        String avatarImage,
        String createdDate,
        String feedContent,
        Integer likeCount,
        Boolean isLiked,
        Integer commentCount,
        Long novelId,
        String title,
        Integer novelRatingCount,
        Float novelRating,
        List<String> relevantCategories,
        Boolean isSpoiler,
        Boolean isModified,
        Boolean isMyFeed

) {
    public static FeedGetResponse of(Feed feed, UserBasicInfo userBasicInfo, Novel novel, Boolean isLiked,
                                     List<String> relevantCategories, Boolean isMyFeed) {
        String title = null;
        Integer novelRatingCount = null;
        Float novelRating = null;

        if (novel != null) {
            title = novel.getTitle();
            novelRatingCount = novel.getNovelRatingCount();
            novelRating = calculateNovelRating(novel.getNovelRatingSum(), novelRatingCount);
        }

        return new FeedGetResponse(
                userBasicInfo.userId(),
                userBasicInfo.nickname(),
                userBasicInfo.avatarImage(),
                feed.getCreatedDate().format(DateTimeFormatter.ofPattern("M월 d일")),
                feed.getFeedContent(),
                feed.getLikeCount(),
                isLiked,
                feed.getCommentCount(),
                feed.getNovelId(),
                title,
                novelRatingCount,
                novelRating,
                relevantCategories,
                feed.getIsSpoiler(),
                !feed.getCreatedDate().equals(feed.getModifiedDate()),
                isMyFeed
        );
    }

    private static Float calculateNovelRating(Float novelRatingSum, Integer novelRatingCount) {
        if (novelRatingCount == 0) {
            return 0.0f;
        }
        return Math.round((novelRatingSum / (float) novelRatingCount) * 10) / 10.0f;
    }

}
