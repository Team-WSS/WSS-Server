package org.websoso.WSSServer.dto.popularNovel;

import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.novel.domain.Novel;
import java.util.List;
import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;

public record PopularNovelGetResponse(
        Long novelId,
        String title,
        String novelImage,
        String avatarImage,
        @SensitiveData(MaskingPolicy.NAME)
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
                    novel.getNovelDescription(),
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
