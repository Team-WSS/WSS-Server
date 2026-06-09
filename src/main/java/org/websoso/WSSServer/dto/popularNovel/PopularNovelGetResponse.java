package org.websoso.WSSServer.dto.popularNovel;

import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.novel.domain.Novel;
import java.util.List;

public record PopularNovelGetResponse(
        Long novelId,
        String title,
        String novelImage,
        String avatarImage,
        String nickname,
        String feedContent,
        List<String> keywords,
        String author,
        String genreName,
        String novelDescription,
        boolean isNovelCompleted

) {

    public static PopularNovelGetResponse of(Novel novel, AvatarProfile avatarProfile, Feed feed, List<String> keywords) {
        if (avatarProfile == null && feed == null) {
            return new PopularNovelGetResponse(
                    novel.getNovelId(),
                    novel.getTitle(),
                    novel.getNovelImage(),
                    null,
                    null,
                    null,
                    keywords,
                    novel.getAuthor(),
                    novel.getFirstGenreName(),
                    novel.getNovelDescription(),
                    novel.getIsCompleted()

            );
        }
        return new PopularNovelGetResponse(
                novel.getNovelId(),
                novel.getTitle(),
                novel.getNovelImage(),
                avatarProfile.getAvatarProfileImage(),
                feed.getUser().getNickname(),
                feed.getFeedContent(),
                keywords,
                novel.getAuthor(),
                novel.getFirstGenreName(),
                novel.getNovelDescription(),
                novel.getIsCompleted()
        );
    }
}
