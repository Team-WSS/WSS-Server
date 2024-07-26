package org.websoso.WSSServer.dto.popularNovel;

import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Novel;

public record PopularNovelGetResponse(
        Long novelId,
        String title,
        String novelImage,
        String avatarImage,
        String nickname,
        String feedContent
) {

    public static PopularNovelGetResponse of(Novel novel, Avatar avatar, Feed feed) {
        return new PopularNovelGetResponse(
                novel.getNovelId(),
                novel.getTitle(),
                novel.getNovelImage(),
                avatar.getAvatarImage(),
                feed.getUser().getNickname(),
                feed.getFeedContent()
        );
    }
}
