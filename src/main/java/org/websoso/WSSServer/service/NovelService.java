package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.novel.NovelErrorCode.NOVEL_NOT_FOUND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelGenre;
import org.websoso.WSSServer.domain.NovelKeywords;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.domain.Platform;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.Keyword.KeywordCountGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponse1;
import org.websoso.WSSServer.dto.novel.NovelGetResponse2;
import org.websoso.WSSServer.dto.platform.PlatformGetResponse;
import org.websoso.WSSServer.exception.novel.exception.InvalidNovelException;
import org.websoso.WSSServer.exception.platform.PlatformErrorCode;
import org.websoso.WSSServer.exception.platform.exception.InvalidPlatformException;
import org.websoso.WSSServer.repository.KeywordRepository;
import org.websoso.WSSServer.repository.NovelKeywordRepository;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.PlatformRepository;

@Service
@RequiredArgsConstructor
public class NovelService {

    private final NovelRepository novelRepository;
    private final PlatformRepository platformRepository;
    private final KeywordRepository keywordRepository;
    private final NovelKeywordRepository novelKeywordRepository;
    private final NovelStatisticsService novelStatisticsService;
    private final UserNovelService userNovelService;

    public NovelGetResponse1 getNovelInfo1(User user, Long novelId) {
        Novel novel = getNovelOrException(novelId);
        List<NovelGenre> novelGenres = novel.getNovelGenres();
        return NovelGetResponse1.of(
                novel,
                userNovelService.getUserNovelOrNull(user, novel),
                novelStatisticsService.getNovelStatisticsOrException(novel),
                getNovelGenreNames(novelGenres),
                getRandomNovelGenreImage(novelGenres)
        );
    }

    public Novel getNovelOrException(Long novelId) {
        return novelRepository.findById(novelId)
                .orElseThrow(() -> new InvalidNovelException(NOVEL_NOT_FOUND,
                        "novel with the given id is not found"));
    }

    private String getNovelGenreNames(List<NovelGenre> novelGenres) {
        return novelGenres.stream().map(novelGenre -> novelGenre.getGenre().getGenreName())
                .collect(Collectors.joining("/"));
    }

    private String getRandomNovelGenreImage(List<NovelGenre> novelGenres) {
        Random random = new Random();
        return novelGenres.get(random.nextInt(novelGenres.size())).getGenre().getGenreImage();
    }

    public NovelGetResponse2 getNovelInfo2(Long novelId) {
        Novel novel = getNovelOrException(novelId);
        NovelStatistics novelStatistics = novelStatisticsService.getNovelStatisticsOrException(novel);
        return NovelGetResponse2.of(
                novel,
                novelStatistics,
                getPlatformResponses(novel),
                getAttractivePoints(novelStatistics),
                getKeywordResponses(novelId)
        );
    }

    private List<PlatformGetResponse> getPlatformResponses(Novel novel) {
        List<Platform> platforms = platformRepository.findAllByNovel(novel);
        if (platforms.isEmpty()) {
            throw new InvalidPlatformException(PlatformErrorCode.PLATFORM_NOT_FOUND,
                    "platform with the given novel is not found");
        }
        return platforms.stream().map(PlatformGetResponse::of).collect(Collectors.toList());
    }

    private List<String> getAttractivePoints(NovelStatistics novelStatistics) {
        Map<String, Integer> attractivePointsMap = makeAttractivePointsMapExcludingZero(novelStatistics);

        if (attractivePointsMap.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, List<String>> groupedByValue = groupAttractivePointsByValue(attractivePointsMap);

        List<String> result = new ArrayList<>();
        List<Integer> sortedKeys = new ArrayList<>(groupedByValue.keySet());
        Collections.sort(sortedKeys, Collections.reverseOrder());

        Random random = new Random();

        for (Integer key : sortedKeys) {
            List<String> items = groupedByValue.get(key);
            if (result.size() + items.size() > 3) {
                Collections.shuffle(items, random);
                items = items.subList(0, 3 - result.size());
            }
            result.addAll(items);
            if (result.size() >= 3) {
                break;
            }
        }

        return result;
    }

    private Map<String, Integer> makeAttractivePointsMapExcludingZero(NovelStatistics novelStatistics) {
        Map<String, Integer> attractivePointsMap = new HashMap<>();

        attractivePointsMap.put("세계관", novelStatistics.getUniverseCount());
        attractivePointsMap.put("분위기", novelStatistics.getVibeCount());
        attractivePointsMap.put("소재", novelStatistics.getMaterialCount());
        attractivePointsMap.put("캐릭터", novelStatistics.getCharactersCount());
        attractivePointsMap.put("관계", novelStatistics.getRelationshipCount());

        attractivePointsMap.entrySet().removeIf(entry -> entry.getValue() == 0);

        return attractivePointsMap;
    }

    private Map<Integer, List<String>> groupAttractivePointsByValue(Map<String, Integer> attractivePointsMap) {
        Map<Integer, List<String>> groupedByValue = new HashMap<>();

        for (Map.Entry<String, Integer> entry : attractivePointsMap.entrySet()) {
            groupedByValue
                    .computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                    .add(entry.getKey());
        }

        return groupedByValue;
    }

    private List<KeywordCountGetResponse> getKeywordResponses(Long novelId) {
        //TODO 아예 없는 경우 : 빈배열
        //TODO 등록한 개수가 많은 5개 키워드를 순서대로 노출
        List<NovelKeywords> novelKeywords = novelKeywordRepository.findAllByNovelId(novelId);
        Map<Integer, Long> keywordFrequencyMap = novelKeywords.stream()
                .collect(Collectors.groupingBy(NovelKeywords::getKeywordId, Collectors.counting()));
        List<Integer> keywordIds = new ArrayList<>(keywordFrequencyMap.keySet());
        List<Keyword> keywords = keywordRepository.findAllById(keywordIds);
        return keywords.stream()
                .map(keyword -> KeywordCountGetResponse.of(
                        keyword,
                        keywordFrequencyMap.getOrDefault(keyword.getKeywordId(), 0L).intValue()))
                .collect(Collectors.toList());
    }

}
