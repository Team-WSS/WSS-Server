package org.websoso.WSSServer.dto.popularNovel;

import java.util.List;
import java.util.Map;
import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.novel.domain.Novel;

public record PopularNovelsGetResponse(
        List<PopularNovelGetResponse> popularNovels
) {
    public static PopularNovelsGetResponse create(List<Novel> popularNovels,
                                                                           Map<Long, Feed> feedMap,
                                                                           Map<Long, AvatarProfile> avatarMap) {
        List<PopularNovelGetResponse> popularNovelResponses = popularNovels.stream()
                .map(novel -> {
                    Feed feed = feedMap.get(novel.getNovelId());
                    if (feed == null) {
                        return PopularNovelGetResponse.of(novel, null, null);
                    }
                    AvatarProfile avatar = avatarMap.get(feed.getUser().getAvatarProfileId());
                    return PopularNovelGetResponse.of(novel, avatar, feed);
                })
                .toList();
        return new PopularNovelsGetResponse(popularNovelResponses);
    }
}
