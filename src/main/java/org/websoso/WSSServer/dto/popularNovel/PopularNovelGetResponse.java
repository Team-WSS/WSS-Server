package org.websoso.WSSServer.dto.popularNovel;

import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.novel.domain.Novel;

public record PopularNovelGetResponse(
        Long novelId,
        String title,
        String novelImage,
        String avatarImage,
        String nickname,
        String feedContent
) {

    public static PopularNovelGetResponse of(Novel novel, AvatarProfile avatarProfile, Feed feed) {
        if (avatarProfile == null && feed == null) {
            return new PopularNovelGetResponse(
                    novel.getNovelId(),
                    novel.getTitle(),
                    novel.getNovelImage(),
                    null,
                    null,
                    novel.getNovelDescription()
            );
        }
        return new PopularNovelGetResponse(
                novel.getNovelId(),
                novel.getTitle(),
                novel.getNovelImage(),
                avatarProfile.getAvatarProfileImage(),
                feed.getUser().getNickname(),
                feed.getFeedContent()
        );
    }
}
