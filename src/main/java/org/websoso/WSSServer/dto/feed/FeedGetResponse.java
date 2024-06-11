package org.websoso.WSSServer.dto.feed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.dto.User.UserBasicInfo;

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
            novelRating = novelRatingCount == 0 ? 0.0f
                    : Math.round((novel.getNovelRatingSum() / (float) novelRatingCount) * 10) / 10.0f;
        }

        return new FeedGetResponse(
                userBasicInfo.userId(),
                userBasicInfo.nickname(),
                userBasicInfo.avatarImage(),
                feed.getCreatedDate().format(DateTimeFormatter.ofPattern("M월 d일")), // TODO : 형식 포맷팅
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
}
