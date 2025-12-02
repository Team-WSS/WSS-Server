package org.websoso.WSSServer.library.service;

import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.NOT_INTERESTED;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_NOT_FOUND;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.AttractivePointName;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.dto.keyword.KeywordCountGetResponse;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelKeyword;
import org.websoso.WSSServer.library.repository.UserNovelAttractivePointRepository;
import org.websoso.WSSServer.library.repository.UserNovelKeywordRepository;
import org.websoso.WSSServer.library.repository.UserNovelRepository;
import org.websoso.WSSServer.novel.domain.Novel;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private static final int KEYWORD_SIZE = 5;
    private static final int ATTRACTIVE_POINT_SIZE = 3;

    private final UserNovelRepository userNovelRepository;
    private final UserNovelKeywordRepository userNovelKeywordRepository;
    private final UserNovelAttractivePointRepository userNovelAttractivePointRepository;

    // TODO: novelId로 불러옴
    @Transactional(readOnly = true)
    public UserNovel getUserNovelOrException(User user, Long novelId) {
        return userNovelRepository.findByNovel_NovelIdAndUser(novelId, user)
                .orElseThrow(() -> new CustomUserNovelException(USER_NOVEL_NOT_FOUND,
                        "user novel with the given user and novel is not found"));
    }

    // TODO: Novel 객체로 불러옴
    // TODO: 사용자 객체가 Null이면 Null로 반환함 헷갈리수도?
    @Transactional(readOnly = true)
    public UserNovel getUserNovelOrNull(User user, Novel novel) {
        if (user == null) {
            return null;
        }
        return userNovelRepository.findByNovelAndUser(novel, user).orElse(null);
    }

    @Transactional
    public UserNovel createLibrary(ReadStatus status, Float userNovelRating, LocalDate startDate, LocalDate endDate,
                                   User user, Novel novel) {
        return userNovelRepository.save(UserNovel.create(
                status,
                userNovelRating,
                startDate,
                endDate,
                user,
                novel));
    }

    public void delete(UserNovel library) {
        userNovelRepository.delete(library);
    }

    public int getRatingCount(Novel novel) {
        return userNovelRepository.countByNovelAndUserNovelRatingNot(novel, 0.0f);
    }

    public float getRatingSum(Novel novel) {
        return userNovelRepository.sumUserNovelRatingByNovel(novel);
    }

    public int getInterestCount(Novel novel) {
        return userNovelRepository.countByNovelAndIsInterestTrue(novel);
    }

    public List<UserNovelKeyword> getKeywords(Novel novel) {
        return userNovelKeywordRepository.findAllByUserNovel_Novel(novel);
    }

    public int getWatchingCount(Novel novel) {
        return userNovelRepository.countByNovelAndStatus(novel, WATCHING);
    }

    public int getWatchedCount(Novel novel) {
        return userNovelRepository.countByNovelAndStatus(novel, WATCHED);
    }

    public int getQuitCount(Novel novel) {
        return userNovelRepository.countByNovelAndStatus(novel, QUIT);
    }

    public int getAttractivePointCount(Novel novel, AttractivePointName point) {
        return userNovelAttractivePointRepository.countByUserNovel_NovelAndAttractivePoint_AttractivePointName(
                novel, point.getLabel());
    }

    public List<Novel> getTasteNovels(List<Genre> preferGenres) {
        return userNovelRepository.findTasteNovels(preferGenres);
    }

    public List<KeywordCountGetResponse> getKeywordNameAndCount(Novel novel) {
        List<UserNovelKeyword> userNovelKeywords = getKeywords(novel);

        if (userNovelKeywords.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Keyword, Long> keywordFrequencyMap = userNovelKeywords.stream()
                .collect(Collectors.groupingBy(UserNovelKeyword::getKeyword, Collectors.counting()));

        return keywordFrequencyMap.entrySet().stream()
                .sorted(Map.Entry.<Keyword, Long>comparingByValue().reversed())
                .limit(KEYWORD_SIZE)
                .map(entry -> KeywordCountGetResponse.of(entry.getKey(), entry.getValue().intValue()))
                .toList();
    }

    public List<String> getAttractivePoints(Novel novel) {
        Map<String, Integer> attractivePointMap = makeAttractivePointMapExcludingZero(novel);

        if (attractivePointMap.isEmpty()) {
            return Collections.emptyList();
        }

        return getTOP3AttractivePoints(attractivePointMap);
    }

    public List<Long> getTodayPopularNovelIds(PageRequest pageRequest) {
        return userNovelRepository.findTodayPopularNovelsId(pageRequest);
    }

    private Map<String, Integer> makeAttractivePointMapExcludingZero(Novel novel) {
        Map<String, Integer> attractivePointMap = new HashMap<>();

        for (AttractivePointName point : AttractivePointName.values()) {
            attractivePointMap.put(point.getLabel(), getAttractivePointCount(novel, point));
        }

        attractivePointMap.entrySet().removeIf(entry -> entry.getValue() == 0);

        return attractivePointMap;
    }

    private List<String> getTOP3AttractivePoints(Map<String, Integer> attractivePointMap) {
        Map<Integer, List<String>> groupedByValue = groupAttractivePointByValue(attractivePointMap);

        List<String> result = new ArrayList<>();
        List<Integer> sortedKeys = new ArrayList<>(groupedByValue.keySet());
        sortedKeys.sort(Collections.reverseOrder());

        Random random = new Random();

        for (Integer key : sortedKeys) {
            List<String> items = groupedByValue.get(key);
            if (result.size() + items.size() > ATTRACTIVE_POINT_SIZE) {
                Collections.shuffle(items, random);
                items = items.subList(0, ATTRACTIVE_POINT_SIZE - result.size());
            }
            result.addAll(items);
            if (result.size() >= ATTRACTIVE_POINT_SIZE) {
                break;
            }
        }

        return result;
    }

    private Map<Integer, List<String>> groupAttractivePointByValue(Map<String, Integer> attractivePointMap) {
        Map<Integer, List<String>> groupedByValue = new HashMap<>();

        for (Map.Entry<String, Integer> entry : attractivePointMap.entrySet()) {
            groupedByValue
                    .computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                    .add(entry.getKey());
        }

        return groupedByValue;
    }
}
