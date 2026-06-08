package org.websoso.WSSServer.dto.popularNovel;

import org.websoso.WSSServer.novel.domain.NovelGenre;
import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.novel.domain.Novel;

import java.util.List;
import java.util.Optional;

public record PopularNovelGetResponse(
        Long novelId,
        String title,
        String novelImage,
        String avatarImage,
        String nickname,
        String feedContent,
        List<String> keywords,
        String author,
        Optional<String> genreName,
        String novelDescription,
        boolean isNovelCompleted,
        List<String> novelGenres

) {

    public static PopularNovelGetResponse of(Novel novel, AvatarProfile avatarProfile, Feed feed, List<String> keywords, List<String> genres) {
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
                    novel.getIsCompleted(),
                    genres

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
                novel.getIsCompleted(),
                genres
        );
    }
}
