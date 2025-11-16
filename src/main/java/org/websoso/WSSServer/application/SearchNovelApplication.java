package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.AttractivePointName;
import org.websoso.WSSServer.domain.common.GenreName;
import org.websoso.WSSServer.dto.keyword.KeywordCountGetResponse;
import org.websoso.WSSServer.dto.novel.FilteredNovelsGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponseBasic;
import org.websoso.WSSServer.dto.novel.NovelGetResponseInfoTab;
import org.websoso.WSSServer.dto.novel.NovelGetResponsePreview;
import org.websoso.WSSServer.dto.novel.SearchedNovelsGetResponse;
import org.websoso.WSSServer.dto.userNovel.TasteNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.TasteNovelsGetResponse;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelKeyword;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelGenre;
import org.websoso.WSSServer.novel.service.GenreServiceImpl;
import org.websoso.WSSServer.novel.service.KeywordServiceImpl;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;

@Service
@RequiredArgsConstructor
public class SearchNovelApplication {

    private static final int ATTRACTIVE_POINT_SIZE = 3;
    private static final int KEYWORD_SIZE = 5;

    private final NovelServiceImpl novelService;
    private final GenreServiceImpl genreService;
    private final KeywordServiceImpl keywordService;
    private final LibraryService libraryService;

    // TODO: 삭제될 레포지토리 의존성
    private final FeedRepository feedRepository;
    private final GenrePreferenceRepository genrePreferenceRepository;

    /**
     * 검색어(소셜명, 작가명)에 해당하는 소설 찾기
     *
     * @param query 검색할 작품명 or 작가명
     * @param page  페이지 네이션 페이지
     * @param size  페이지 네이션 사이즈
     * @return SearchedNovelsGetResponse
     */
    @Transactional(readOnly = true)
    public SearchedNovelsGetResponse searchNovels(String query, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        String searchQuery = query.replaceAll("\\s+", "").replaceAll("[^a-zA-Z0-9가-힣]", "");

        if (searchQuery.isBlank()) {
            return SearchedNovelsGetResponse.of(0L, false, Collections.emptyList());
        }

        Page<Novel> novels = novelService.searchNovels(pageRequest, searchQuery);

        List<NovelGetResponsePreview> novelGetResponsePreviews = novels.stream()
                .map(this::convertToDTO)
                .toList();

        return SearchedNovelsGetResponse.of(novels.getTotalElements(), novels.hasNext(), novelGetResponsePreviews);
    }

    @Transactional(readOnly = true)
    public FilteredNovelsGetResponse getFilteredNovels(List<String> genreNames, Boolean isCompleted, Float novelRating,
                                                       List<Integer> keywordIds, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Genre> genres = getGenres(genreNames);
        List<Keyword> keywords = getKeywords(keywordIds);

        Page<Novel> novels = novelService.findFilteredNovels(pageRequest, genres, keywords, isCompleted, novelRating);

        List<NovelGetResponsePreview> novelGetResponsePreviews = novels.stream()
                .map(this::convertToDTO)
                .toList();

        return FilteredNovelsGetResponse.of(novels.getTotalElements(), novels.hasNext(), novelGetResponsePreviews);
    }

    @Transactional(readOnly = true)
    public NovelGetResponseBasic getNovelInfoBasic(User user, Long novelId) {
        Novel novel = novelService.getNovelOrException(novelId);

        // TODO: Novel에 List<NovelGenre> 있는데 굳이?
        List<NovelGenre> novelGenres = novelService.getGenresByNovel(novel);

        int novelRatingCount = libraryService.getRatingCount(novel);

        Float novelRating = novelRatingCount == 0 ? 0.0f
                : Math.round(libraryService.getRatingSum(novel) / novelRatingCount * 10.0f) / 10.0f;
        return NovelGetResponseBasic.of(
                novel,
                libraryService.getUserNovelOrNull(user, novel),
                getNovelGenreNames(novelGenres),
                getRandomNovelGenreImage(novelGenres),
                libraryService.getInterestCount(novel),
                novelRating,
                novelRatingCount,
                feedRepository.countByNovelId(novelId)
        );
    }

    @Transactional(readOnly = true)
    public NovelGetResponseInfoTab getNovelInfoInfoTab(Long novelId) {
        Novel novel = novelService.getNovelOrException(novelId);

        return NovelGetResponseInfoTab.of(
                novel,
                novelService.getPlatforms(novel),
                getAttractivePoints(novel),
                getKeywords2(novel),
                libraryService.getWatchingCount(novel),
                libraryService.getWatchedCount(novel),
                libraryService.getQuitCount(novel)
        );
    }

    @Transactional(readOnly = true)
    public TasteNovelsGetResponse getTasteNovels(User user) {
        // TODO: 선호하는 장르 리스트는 유저에서 가져오도록 해야함
        List<Genre> preferGenres = genrePreferenceRepository.findByUser(user)
                .stream()
                .map(GenrePreference::getGenre)
                .toList();

        List<Novel> tasteNovels = libraryService.getTasteNovels(preferGenres);

        List<TasteNovelGetResponse> tasteNovelGetResponses = tasteNovels.stream()
                .map(TasteNovelGetResponse::of)
                .toList();

        return TasteNovelsGetResponse.of(tasteNovelGetResponses);
    }

    private NovelGetResponsePreview convertToDTO(Novel novel) {
        // TODO: UserNovel 리스트 개수를 세는것이 아닌, 개수를 세는 쿼리가 필요
        List<UserNovel> userNovels = novel.getUserNovels();

        long interestCount = userNovels.stream()
                .filter(UserNovel::getIsInterest)
                .count();
        long novelRatingCount = userNovels.stream()
                .filter(un -> un.getUserNovelRating() != 0.0f)
                .count();
        double novelRatingSum = userNovels.stream()
                .filter(un -> un.getUserNovelRating() != 0.0f)
                .mapToDouble(UserNovel::getUserNovelRating)
                .sum();

        Float novelRatingAverage = novelRatingCount == 0
                ? 0.0f
                : Math.round((float) (novelRatingSum / novelRatingCount) * 10.0f) / 10.0f;

        return NovelGetResponsePreview.of(
                novel,
                interestCount,
                novelRatingAverage,
                novelRatingCount
        );
    }

    // TODO: for 문으로 장르를 데이터베이스에서 읽어오는 부분 개선해야함
    private List<Genre> getGenres(List<String> genreNames) {
        genreNames = genreNames == null
                ? Collections.emptyList()
                : genreNames;

        List<Genre> genres = new ArrayList<>();
        if (!genreNames.isEmpty()) {
            for (String genreName : genreNames) {
                genres.add(genreService.getGenreOrException(genreName));
            }
        }

        return genres;
    }

    // TODO: for 문으로 키워드를 데이터베이스에서 읽어오는 부분 개선해야함
    private List<Keyword> getKeywords(List<Integer> keywordIds) {
        keywordIds = keywordIds == null
                ? Collections.emptyList()
                : keywordIds;

        List<Keyword> keywords = new ArrayList<>();
        if (!keywordIds.isEmpty()) {
            for (Integer keywordId : keywordIds) {
                keywords.add(keywordService.getKeywordOrException(keywordId));
            }
        }

        return keywords;
    }

    private List<KeywordCountGetResponse> getKeywords2(Novel novel) {
        List<UserNovelKeyword> userNovelKeywords = libraryService.getKeywords(novel);

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

    private String getNovelGenreNames(List<NovelGenre> novelGenres) {
        return novelGenres.stream()
                .map(novelGenre -> getKoreanGenreName(novelGenre.getGenre().getGenreName()))
                .collect(Collectors.joining("/"));
    }

    private String getKoreanGenreName(String englishGenreName) {
        return Arrays.stream(GenreName.values())
                .filter(genreName -> genreName.getLabel().equalsIgnoreCase(englishGenreName))
                .findFirst()
                .map(GenreName::getKoreanLabel)
                .orElseThrow(
                        () -> new CustomGenreException(GENRE_NOT_FOUND, "Genre with the given genreName is not found"));
    }

    private String getRandomNovelGenreImage(List<NovelGenre> novelGenres) {
        Random random = new Random();
        return novelGenres.get(random.nextInt(novelGenres.size())).getGenre().getGenreImage();
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
            attractivePointMap.put(point.getLabel(), libraryService.getAttractivePointCount(novel, point));
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
