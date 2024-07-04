package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.keyword.KeywordErrorCode.KEYWORD_NOT_FOUND;
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
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelGenre;
import org.websoso.WSSServer.domain.NovelKeywords;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.domain.Platform;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.keyword.KeywordCountGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponse1;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.dto.novel.NovelGetResponse2;
import org.websoso.WSSServer.dto.platform.PlatformGetResponse;
import org.websoso.WSSServer.exception.keyword.exception.InvalidKeywordException;
import org.websoso.WSSServer.exception.novel.exception.InvalidNovelException;
import org.websoso.WSSServer.repository.KeywordRepository;
import org.websoso.WSSServer.repository.NovelKeywordRepository;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.PlatformRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class NovelService {

    private final NovelRepository novelRepository;
    private final PlatformRepository platformRepository;
    private final KeywordRepository keywordRepository;
    private final NovelKeywordRepository novelKeywordRepository;
    private final NovelStatisticsService novelStatisticsService;
    private final UserNovelService userNovelService;

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public Novel getNovelOrException(Long novelId) {
        return novelRepository.findById(novelId)
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND,
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
                getPlatforms(novel),
                getAttractivePoints(novelStatistics),
                getKeywords(novelId)
        );
    }

    private List<PlatformGetResponse> getPlatforms(Novel novel) {
        List<Platform> platforms = platformRepository.findAllByNovel(novel);
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

    private List<KeywordCountGetResponse> getKeywords(Long novelId) {
        List<NovelKeywords> novelKeywords = novelKeywordRepository.findAllByNovelId(novelId);

        if (novelKeywords.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, Long> keywordFrequencyMap = novelKeywords.stream()
                .collect(Collectors.groupingBy(NovelKeywords::getKeywordId, Collectors.counting()));

        return keywordFrequencyMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Keyword keyword = keywordRepository.findById(entry.getKey()).orElseThrow(
                            () -> new InvalidKeywordException(KEYWORD_NOT_FOUND,
                                    "keyword with the given id is not found"));
                    return KeywordCountGetResponse.of(keyword, entry.getValue().intValue());
                })
                .collect(Collectors.toList());
    }

}
