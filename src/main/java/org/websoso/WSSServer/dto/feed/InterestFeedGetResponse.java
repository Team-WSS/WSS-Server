package org.websoso.WSSServer.dto.feed;

import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.AvatarProfile;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.library.domain.UserNovel;

public record InterestFeedGetResponse(
        Long novelId,
        String novelTitle,
        String novelImage,
        Float novelRating,
        Long novelRatingCount,
        Boolean isPublic,
        String nickname,
        String avatarImage,
        String feedContent
) {

    public static InterestFeedGetResponse of(Novel novel, User user, Feed feed, AvatarProfile avatarProfile) {
        Long novelRatingCount = getNovelRatingCount(novel);
        Float novelRating = getNovelRating(novel, novelRatingCount);

        return new InterestFeedGetResponse(
                novel.getNovelId(),
                novel.getTitle(),
                novel.getNovelImage(),
                novelRating,
                novelRatingCount,
                feed.getIsPublic(),
                user.getNickname(),
                avatarProfile.getAvatarProfileImage(),
                feed.getFeedContent()
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
