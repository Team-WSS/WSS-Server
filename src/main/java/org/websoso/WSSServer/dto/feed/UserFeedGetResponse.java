package org.websoso.WSSServer.dto.feed;

import java.time.LocalDate;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovel;

public record UserFeedGetResponse(
        Long feedId,
        String feedContent,
        LocalDate createdDate,
        Boolean isSpoiler,
        Boolean isModified,
//        List<Long> likerUsers,
//        Integer likeCount,
//        Integer commentCount,
        Long novelId,
        String title
//        Float novelRating,
//        Long novelRatingCount
//        List<String> relevantCategories
) {

    public static UserFeedGetResponse of(Feed feed, Novel novel) {

        boolean isModified = !feed.getCreatedDate().equals(feed.getModifiedDate());

        return new UserFeedGetResponse(
                feed.getFeedId(),
                feed.getFeedContent(),
                feed.getCreatedDate().toLocalDate(),
                feed.getIsSpoiler(),
                isModified,
                novel.getNovelId(),
                novel.getTitle()
        );
    }

    private static Long getNovelRatingCount(Novel novel) {
        return novel.getUserNovels()
                .stream()
                .filter(userNovel -> userNovel.getUserNovelRating() != 0.0f)
                .count();
    }

    private static Float getNovelRatingSum(Novel novel) {
        return (float) novel.getUserNovels()
                .stream()
                .filter(userNovel -> userNovel.getUserNovelRating() != 0.0f)
                .mapToDouble(UserNovel::getUserNovelRating)
                .sum();
    }

    private static float getNovelRating(Novel novel, Long novelRatingCount) {
        return novelRatingCount > 0
                ? getNovelRatingSum(novel) / novelRatingCount
                : 0.0f;
    }
}
