package org.websoso.WSSServer.dto.feed;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.FeedImage;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.dto.user.UserBasicInfo;

public record FeedGetResponse(
        Long userId,
        String nickname,
        String avatarImage,
        Long feedId,
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
        List<String> images,
        String novelThumbnailImage,
        String novelGenre,
        String novelAuthor,
        Float feedWriterNovelRating,
        String novelDescription
) {
    public static FeedGetResponse of(Feed feed, UserBasicInfo feedUserBasicInfo, Novel novel, Boolean isLiked,
                                     List<String> relevantCategories, Boolean isMyFeed, User user) {
        String title = null;
        Integer novelRatingCount = null;
        Float novelRating = null;
        String novelThumbnailImage = null;
        String novelGenre = null;
        String novelAuthor = null;
        Float feedWriterNovelRating = null;
        String novelDescription = null;

        if (novel != null) {
            List<UserNovel> userNovels = novel.getUserNovels().stream()
                    .filter(un -> un.getUserNovelRating() > 0.0)
                    .toList();
            title = novel.getTitle();
            novelRatingCount = userNovels.size();
            novelRating = calculateNovelRating(
                    (float) userNovels.stream()
                            .map(UserNovel::getUserNovelRating)
                            .mapToDouble(d -> d).sum(),
                    novelRatingCount);
            novelThumbnailImage = novel.getNovelImage();
            novelGenre = novel.getNovelGenres().get(0).getGenre().getGenreName();
            novelAuthor = novel.getAuthor();
            feedWriterNovelRating = getFeedWriterNovelRating(novel, feed.getUser().getUserId());
            novelDescription = novel.getNovelDescription();
        }

        List<String> imageUrls = feed.getImages().stream()
                .map(FeedImage::getUrl)
                .toList();

        return new FeedGetResponse(
                feedUserBasicInfo.userId(),
                feedUserBasicInfo.nickname(),
                feedUserBasicInfo.avatarImage(),
                feed.getFeedId(),
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
                imageUrls,
                novelThumbnailImage,
                novelGenre,
                novelAuthor,
                feedWriterNovelRating,
                novelDescription
        );
    }

    private static Float calculateNovelRating(Float novelRatingSum, Integer novelRatingCount) {
        if (novelRatingCount == 0) {
            return 0.0f;
        }
        return Math.round((novelRatingSum / (float) novelRatingCount) * 10) / 10.0f;
    }

    private static Float getFeedWriterNovelRating(Novel novel, Long feedWriterId) {
        if (novel == null) {
            return null;
        }

        return novel.getUserNovels()
                .stream()
                .filter(userNovel -> userNovel.getUser().getUserId().equals(feedWriterId))
                .findFirst()
                .map(UserNovel::getUserNovelRating)
                .orElse(null);
    }
}
