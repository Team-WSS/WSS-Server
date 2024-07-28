package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.ALREADY_INTERESTED;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.NOT_INTERESTED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelGenre;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.UserNovelKeyword;
import org.websoso.WSSServer.domain.common.AttractivePointName;
import org.websoso.WSSServer.dto.keyword.KeywordCountGetResponse;
import org.websoso.WSSServer.dto.novel.FilteredNovelsGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponseBasic;
import org.websoso.WSSServer.dto.novel.NovelGetResponseInfoTab;
import org.websoso.WSSServer.dto.novel.NovelGetResponsePreview;
import org.websoso.WSSServer.dto.platform.PlatformGetResponse;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.repository.FeedRepository;
import org.websoso.WSSServer.repository.NovelGenreRepository;
import org.websoso.WSSServer.repository.NovelPlatformRepository;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.UserNovelAttractivePointRepository;
import org.websoso.WSSServer.repository.UserNovelKeywordRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class NovelService {

    private static final int ATTRACTIVE_POINT_SIZE = 3;
    private static final int KEYWORD_SIZE = 5;

    private final NovelRepository novelRepository;
    private final UserNovelService userNovelService;
    private final UserNovelRepository userNovelRepository;
    private final NovelPlatformRepository novelPlatformRepository;
    private final UserNovelAttractivePointRepository userNovelAttractivePointRepository;
    private final FeedRepository feedRepository;
    private final NovelGenreRepository novelGenreRepository;
    private final UserNovelKeywordRepository userNovelKeywordRepository;
    private final KeywordService keywordService;
    private final GenreService genreService;

    @Transactional(readOnly = true)
    public Novel getNovelOrException(Long novelId) {
        return novelRepository.findById(novelId)
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND,
                        "novel with the given id is not found"));
    }

    @Transactional(readOnly = true)
    public NovelGetResponseBasic getNovelInfoBasic(User user, Long novelId) {
        Novel novel = getNovelOrException(novelId);
        List<NovelGenre> novelGenres = novelGenreRepository.findAllByNovel(novel);
        Integer novelRatingCount = userNovelRepository.countByNovelAndUserNovelRatingNot(novel, 0.0f);
        Float novelRating = novelRatingCount == 0 ? 0.0f
                : Math.round(userNovelRepository.sumUserNovelRatingByNovel(novel) / novelRatingCount * 10.0f) / 10.0f;
        return NovelGetResponseBasic.of(
                novel,
                userNovelService.getUserNovelOrNull(user, novel),
                getNovelGenreNames(novelGenres),
                getRandomNovelGenreImage(novelGenres),
                userNovelRepository.countByNovelAndIsInterestTrue(novel),
                novelRating,
                novelRatingCount,
                feedRepository.countByNovelId(novelId)
        );
    }

    private String getNovelGenreNames(List<NovelGenre> novelGenres) {
        return novelGenres.stream().map(novelGenre -> novelGenre.getGenre().getGenreName())
                .collect(Collectors.joining("/"));
    }

    private String getRandomNovelGenreImage(List<NovelGenre> novelGenres) {
        Random random = new Random();
        return novelGenres.get(random.nextInt(novelGenres.size())).getGenre().getGenreImage();
    }

    public void registerAsInterest(User user, Long novelId) {

        Novel novel = getNovelOrException(novelId);
        UserNovel userNovel = userNovelService.getUserNovelOrNull(user, novel);

        if (userNovel != null && userNovel.getIsInterest()) {
            throw new CustomUserNovelException(ALREADY_INTERESTED, "already registered as interested");
        }

        if (userNovel == null) {
            userNovel = userNovelService.createUserNovelByInterest(user, novel);
        }

        userNovel.setIsInterest(true);
    }

    public void unregisterAsInterest(User user, Long novelId) {

        Novel novel = getNovelOrException(novelId);
        UserNovel userNovel = userNovelService.getUserNovelOrException(user, novel);

        if (!userNovel.getIsInterest()) {
            throw new CustomUserNovelException(NOT_INTERESTED, "not registered as interest");
        }

        userNovel.setIsInterest(false);

        if (isUserNovelOnlyByInterest(userNovel)) {
            userNovelRepository.delete(userNovel);
        }

    }

    private Boolean isUserNovelOnlyByInterest(UserNovel userNovel) {
        return userNovel.getStatus() == null;
    }

    @Transactional(readOnly = true)
    public NovelGetResponseInfoTab getNovelInfoInfoTab(Long novelId) {
        Novel novel = getNovelOrException(novelId);
        return NovelGetResponseInfoTab.of(
                novel,
                getPlatforms(novel),
                getAttractivePoints(novel),
                getKeywords(novel),
                userNovelRepository.countByNovelAndStatus(novel, WATCHING),
                userNovelRepository.countByNovelAndStatus(novel, WATCHED),
                userNovelRepository.countByNovelAndStatus(novel, QUIT)
        );
    }

    private List<PlatformGetResponse> getPlatforms(Novel novel) {
        return novelPlatformRepository.findAllByNovel(novel).stream().map(PlatformGetResponse::of)
                .collect(Collectors.toList());
    }

    private List<String> getAttractivePoints(Novel novel) {

        Map<String, Integer> attractivePointMap = makeAttractivePointMapExcludingZero(novel);

        if (attractivePointMap.isEmpty()) {
            return Collections.emptyList();
        }

        return getTOP3AttractivePoints(attractivePointMap);
    }

    private Map<String, Integer> makeAttractivePointMapExcludingZero(Novel novel) {

        Map<String, Integer> attractivePointMap = new HashMap<>();

        for (AttractivePointName point : AttractivePointName.values()) {
            attractivePointMap.put(point.getLabel(),
                    userNovelAttractivePointRepository.countByUserNovel_NovelAndAttractivePoint_AttractivePointName(
                            novel, point.getLabel()));
        }

        attractivePointMap.entrySet().removeIf(entry -> entry.getValue() == 0);

        return attractivePointMap;
    }

    private List<String> getTOP3AttractivePoints(Map<String, Integer> attractivePointMap) {

        Map<Integer, List<String>> groupedByValue = groupAttractivePointByValue(attractivePointMap);

        List<String> result = new ArrayList<>();
        List<Integer> sortedKeys = new ArrayList<>(groupedByValue.keySet());
        Collections.sort(sortedKeys, Collections.reverseOrder());

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

    private List<KeywordCountGetResponse> getKeywords(Novel novel) {

        List<UserNovelKeyword> userNovelKeywords = userNovelKeywordRepository.findAllByUserNovel_Novel(novel);

        if (userNovelKeywords.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Keyword, Long> keywordFrequencyMap = userNovelKeywords.stream()
                .collect(Collectors.groupingBy(UserNovelKeyword::getKeyword, Collectors.counting()));

        return keywordFrequencyMap.entrySet().stream()
                .sorted(Map.Entry.<Keyword, Long>comparingByValue().reversed())
                .limit(KEYWORD_SIZE)
                .map(entry -> KeywordCountGetResponse.of(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FilteredNovelsGetResponse getFilteredNovels(List<String> genreNames, Boolean isCompleted, Float novelRating,
                                                       List<Integer> keywordIds, int page, int size) {

        List<Genre> genres;
        if (genreNames == null) {
            genres = null;
        } else {
            genres = new ArrayList<>();
            for (String genreName : genreNames) {
                Genre genre = genreService.getGenreOrException(genreName);
                genres.add(genre);
            }
        }

        List<Keyword> keywords;
        if (keywordIds == null) {
            keywords = null;
        } else {
            keywords = new ArrayList<>();
            for (Integer keywordId : keywordIds) {
                Keyword keyword = keywordService.getKeywordOrException(keywordId);
                keywords.add(keyword);
            }
        }

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Novel> novels = novelRepository.findFilteredNovels(pageRequest, genres, isCompleted, novelRating, keywords,
                keywords.size());

        List<NovelGetResponsePreview> novelGetResponsePreviews = novels.stream().map(novel -> {

            List<UserNovel> userNovels = userNovelRepository.findAllByNovel(novel);

            long interestCount = userNovels.stream().filter(UserNovel::getIsInterest).count();
            long novelRatingCount = userNovels.stream().filter(un -> un.getUserNovelRating() != 0.0f).count();
            double novelRatingSum = userNovels.stream().filter(un -> un.getUserNovelRating() != 0.0f)
                    .mapToDouble(UserNovel::getUserNovelRating).sum();
            Float novelRatingAverage = novelRatingCount == 0 ? 0.0f
                    : Math.round((float) (novelRatingSum / novelRatingCount) * 10.0f) / 10.0f;

            return NovelGetResponsePreview.of(
                    novel,
                    (int) interestCount,
                    novelRatingAverage,
                    (int) novelRatingCount
            );
        }).collect(Collectors.toList());

        return FilteredNovelsGetResponse.of(novels.getTotalElements(), novels.hasNext(), novelGetResponsePreviews);
    }

}
