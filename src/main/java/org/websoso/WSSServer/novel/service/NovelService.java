package org.websoso.WSSServer.novel.service;


import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.domain.PopularNovel;
import org.websoso.WSSServer.dto.popularNovel.PopularNovelGetResponse;
import org.websoso.WSSServer.dto.popularNovel.PopularNovelsGetResponse;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.repository.AvatarRepository;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.novel.repository.NovelRepository;
import org.websoso.WSSServer.repository.PopularNovelRepository;
import org.websoso.WSSServer.library.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class NovelService {

    private final NovelRepository novelRepository;
    private final FeedRepository feedRepository;
    private final PopularNovelRepository popularNovelRepository;
    private final AvatarRepository avatarRepository;

    @Transactional(readOnly = true)
    public Novel getNovelOrException(Long novelId) {
        return novelRepository.findById(novelId)
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND,
                        "novel with the given id is not found"));
    }


    @Transactional(readOnly = true)
    public PopularNovelsGetResponse getTodayPopularNovels() {
        List<Long> novelIdsFromPopularNovel = getNovelIdsFromPopularNovel();
        List<Long> selectedNovelIdsFromPopularNovel = getSelectedNovelIdsFromPopularNovel(novelIdsFromPopularNovel);
        List<Novel> popularNovels = getSelectedPopularNovels(selectedNovelIdsFromPopularNovel);
        List<Feed> popularFeedsFromPopularNovels = getPopularFeedsFromPopularNovels(selectedNovelIdsFromPopularNovel);

        Map<Long, Feed> feedMap = createFeedMap(popularFeedsFromPopularNovels);
        Map<Byte, Avatar> avatarMap = createAvatarMap(feedMap);

        return createPopularNovelsGetResponse(popularNovels, feedMap, avatarMap);
    }

    private List<Long> getNovelIdsFromPopularNovel() {
        return new ArrayList<>(popularNovelRepository.findAll()
                .stream()
                .map(PopularNovel::getNovelId)
                .toList());
    }

    private static List<Long> getSelectedNovelIdsFromPopularNovel(List<Long> popularNovelIds) {
        Collections.shuffle(popularNovelIds);
        return popularNovelIds.size() > 10
                ? popularNovelIds.subList(0, 10)
                : popularNovelIds;
    }

    private List<Novel> getSelectedPopularNovels(List<Long> selectedPopularNovelIds) {
        return novelRepository.findAllById(selectedPopularNovelIds);
    }

    private List<Feed> getPopularFeedsFromPopularNovels(List<Long> selectedPopularNovelIds) {
        return feedRepository.findPopularFeedsByNovelIds(selectedPopularNovelIds);
    }

    private static Map<Long, Feed> createFeedMap(List<Feed> popularFeedsFromPopularNovels) {
        return popularFeedsFromPopularNovels.stream()
                .collect(Collectors.toMap(Feed::getNovelId, feed -> feed));
    }

    private Map<Byte, Avatar> createAvatarMap(Map<Long, Feed> feedMap) {
        Set<Byte> avatarIds = feedMap.values()
                .stream()
                .map(feed -> feed.getUser().getAvatarId())
                .collect(Collectors.toSet());

        List<Avatar> avatars = avatarRepository.findAllById(avatarIds);
        return avatars.stream()
                .collect(Collectors.toMap(Avatar::getAvatarId, avatar -> avatar));
    }

    private static PopularNovelsGetResponse createPopularNovelsGetResponse(List<Novel> popularNovels,
                                                                           Map<Long, Feed> feedMap,
                                                                           Map<Byte, Avatar> avatarMap) {
        List<PopularNovelGetResponse> popularNovelResponses = popularNovels.stream()
                .map(novel -> {
                    Feed feed = feedMap.get(novel.getNovelId());
                    if (feed == null) {
                        return PopularNovelGetResponse.of(novel, null, null);
                    }
                    Avatar avatar = avatarMap.get(feed.getUser().getAvatarId());
                    return PopularNovelGetResponse.of(novel, avatar, feed);
                })
                .toList();
        return new PopularNovelsGetResponse(popularNovelResponses);
    }

}
