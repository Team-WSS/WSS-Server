package org.websoso.WSSServer.dto.feed;

import java.time.LocalDate;
import java.util.List;
import org.websoso.WSSServer.domain.Category;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.FeedCategory;
import org.websoso.WSSServer.domain.FeedImage;
import org.websoso.WSSServer.domain.Like;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovel;

public record UserFeedGetResponse(
        Long feedId,
        String feedContent,
        LocalDate createdDate,
        Boolean isSpoiler,
        Boolean isModified,
        List<Long> likerUsers,
        Boolean isLiked,
        Integer likeCount,
        Integer commentCount,
        Long novelId,
        String title,
        Float novelRating,
        Long novelRatingCount,
        List<String> relevantCategories,
        Boolean isPublic,
        String genre,
        Float userNovelRating,
        List<String> feedImages
) {

    public static UserFeedGetResponse of(Feed feed, Novel novel, Long visitorId) {
        boolean isModified = !feed.getCreatedDate().equals(feed.getModifiedDate());
        Long novelRatingCount = getNovelRatingCount(novel);
        Float novelRating = getNovelRating(novel, novelRatingCount);
        List<Long> likeUsers = getLikeUsers(feed);
        boolean isLiked = likeUsers.contains(visitorId);
        List<String> relevantCategories = getFeedCategories(feed);
        String genreName = getNovelGenreName(novel);
        Float userNovelRating = getUserNovelRating(novel, visitorId);
        List<String> feedImages = feed.getImages().stream()
                .map(FeedImage::getUrl)
                .toList();

        return new UserFeedGetResponse(
                feed.getFeedId(),
                feed.getFeedContent(),
                feed.getCreatedDate().toLocalDate(),
                feed.getIsSpoiler(),
                isModified,
                likeUsers,
                isLiked,
                feed.getLikes().size(),
                feed.getComments().size(),
                novel == null ?
                        null : novel.getNovelId(),
                novel == null ?
                        null : novel.getTitle(),
                novelRating,
                novelRatingCount,
                relevantCategories,
                feed.getIsPublic(),
                genreName,
                userNovelRating,
                feedImages
        );
    }

    private static List<String> getFeedCategories(Feed feed) {
        return feed.getFeedCategories()
                .stream()
                .map(FeedCategory::getCategory)
                .map(Category::getCategoryName)
                .map(Enum::name)
                .toList();
    }

    private static List<Long> getLikeUsers(Feed feed) {
        return feed.getLikes()
                .stream()
                .map(Like::getUserId)
                .toList();
    }

    private static Long getNovelRatingCount(Novel novel) {
        if (novel == null) {
            return null;
        }
        return novel.getUserNovels()
                .stream()
                .filter(userNovel -> userNovel.getUserNovelRating() != 0.0f)
                .count();
    }

    private static Float getNovelRatingSum(Novel novel) {
        if (novel == null) {
            return null;
        }

        return (float) novel.getUserNovels()
                .stream()
                .filter(userNovel -> userNovel.getUserNovelRating() != 0.0f)
                .mapToDouble(UserNovel::getUserNovelRating)
                .sum();
    }

    private static Float getNovelRating(Novel novel, Long novelRatingCount) {
        if (novel == null) {
            return null;
        }
        return novelRatingCount > 0
                ? Math.round(getNovelRatingSum(novel) / novelRatingCount * 10) / 10.0f
                : 0.0f;
    }

    private static String getNovelGenreName(Novel novel) {
        if (novel == null) {
            return null;
        }

        return novel.getNovelGenres().get(0).getGenre().getGenreName();
    }

    private static Float getUserNovelRating(Novel novel, Long visitorId) {
        if (novel == null) {
            return null;
        }

        return novel.getUserNovels()
                .stream()
                .filter(userNovel -> userNovel.getUser().getUserId().equals(visitorId))
                .findFirst()
                .map(UserNovel::getUserNovelRating)
                .orElse(null);
    }
}
