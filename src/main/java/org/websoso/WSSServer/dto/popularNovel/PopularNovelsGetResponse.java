package org.websoso.WSSServer.dto.popularNovel;

import java.util.List;
import java.util.Map;

import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.novel.domain.Novel;

public record PopularNovelsGetResponse(
        List<PopularNovelGetResponse> popularNovels
) {
    public static PopularNovelsGetResponse create(List<Novel> popularNovels,
                                                                           Map<Long, Feed> feedMap,
                                                                           Map<Long, AvatarProfile> avatarMap,
                                                                           Map<Long, List<Keyword>> keywords) {
        List<PopularNovelGetResponse> popularNovelResponses = popularNovels.stream()
                .map(novel -> {
                    Feed feed = feedMap.get(novel.getNovelId());

                    List<String> keywordGetResponsesList = keywords.get(novel.getNovelId()).stream()
                            .map(keyword -> keyword.getKeywordName())
                            .toList();
                    List<String> genres = novel.getNovelGenres().stream()
                            .map(novelGenre -> novelGenre.getGenre().getGenreName())
                            .toList();
                    if (feed == null) {
                        return PopularNovelGetResponse.of(novel, null, null, keywordGetResponsesList, genres);
                    }
                    AvatarProfile avatar = avatarMap.get(feed.getUser().getAvatarProfileId());
                    return PopularNovelGetResponse.of(novel, avatar, feed, keywordGetResponsesList, genres);
                })
                .toList();
        return new PopularNovelsGetResponse(popularNovelResponses);
    }
}
