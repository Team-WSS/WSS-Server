package org.websoso.WSSServer.dto.feed;

import java.time.LocalDate;
import org.websoso.WSSServer.domain.Feed;

public record UserFeedGetResponse(
        Long feedId,
        String feedContent,
        LocalDate createdDate,
        Boolean isSpoiler,
        Boolean isModified
//        List<Long> likerUsers,
//        Integer likeCount,
//        Integer commentCount,
//        Long novelId,
//        String title,
//        Float novelRating,
//        Long novelRatingCount
//        List<String> relevantCategories
) {

    public static UserFeedGetResponse of(Feed feed) {

        boolean isModified = !feed.getCreatedDate().equals(feed.getModifiedDate());

        return new UserFeedGetResponse(
                feed.getFeedId(),
                feed.getFeedContent(),
                feed.getCreatedDate().toLocalDate(),
                feed.getIsSpoiler(),
                isModified
        );
    }
}
