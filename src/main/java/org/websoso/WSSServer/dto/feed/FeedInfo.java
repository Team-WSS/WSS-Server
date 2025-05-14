package org.websoso.WSSServer.dto.feed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.FeedImage;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.dto.user.UserBasicInfo;

public record FeedInfo(
        Long feedId,
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
        Boolean isMyFeed,
        Boolean isPublic,
        String thumbnailUrl,
        Integer imageCount
) {
    public static FeedInfo of(Feed feed, UserBasicInfo userBasicInfo, Novel novel, Boolean isLiked,
                              List<String> relevantCategories, Boolean isMyFeed, String thumbnailUrl, Integer imageCount) {
        String title = null;
        Integer novelRatingCount = null;
        Float novelRating = null;

        if (novel != null) {
            List<UserNovel> userNovels = novel.getUserNovels().stream().filter(un -> un.getUserNovelRating() > 0.0)
                    .toList();
            title = novel.getTitle();
            novelRatingCount = userNovels.size();
            novelRating = calculateNovelRating(
                    (float) userNovels.stream().map(UserNovel::getUserNovelRating).mapToDouble(d -> d).sum(),
                    novelRatingCount);
        }

        return new FeedInfo(
                feed.getFeedId(),
                userBasicInfo.userId(),
                userBasicInfo.nickname(),
                userBasicInfo.avatarImage(),
                feed.getCreatedDate().format(DateTimeFormatter.ofPattern("M월 d일")),
                feed.getFeedContent(),
                feed.getLikes().size(),
                isLiked,
                feed.getComments().size(),
                feed.getNovelId(),
                title,
                novelRatingCount,
                novelRating,
                relevantCategories,
                feed.getIsSpoiler(),
                !feed.getCreatedDate().equals(feed.getModifiedDate()),
                isMyFeed,
                feed.getIsPublic(),
                thumbnailUrl,
                imageCount
        );
    }

    private static Float calculateNovelRating(Float novelRatingSum, Integer novelRatingCount) {
        if (novelRatingCount == 0) {
            return 0.0f;
        }
        return Math.round((novelRatingSum / (float) novelRatingCount) * 10) / 10.0f;
    }

}
