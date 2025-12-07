package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.library.service.AttractivePointService;
import org.websoso.WSSServer.library.service.KeywordService;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.domain.common.GenreName;
import org.websoso.WSSServer.dto.novel.FilteredNovelsGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponseBasic;
import org.websoso.WSSServer.dto.novel.NovelGetResponseInfoTab;
import org.websoso.WSSServer.dto.novel.NovelGetResponsePreview;
import org.websoso.WSSServer.dto.novel.SearchedNovelsGetResponse;
import org.websoso.WSSServer.dto.popularNovel.PopularNovelsGetResponse;
import org.websoso.WSSServer.dto.userNovel.TasteNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.TasteNovelsGetResponse;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelGenre;
import org.websoso.WSSServer.novel.service.GenreServiceImpl;
import org.websoso.WSSServer.novel.service.KeywordServiceImpl;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.novel.service.PopularNovelService;
import org.websoso.WSSServer.repository.AvatarRepository;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;

@Service
@RequiredArgsConstructor
public class SearchNovelApplication {

    private final NovelServiceImpl novelService;
    private final PopularNovelService popularNovelService;
    private final GenreServiceImpl genreService;
    private final AttractivePointService libraryAttractivePointService;
    private final KeywordService libraryKeywordService;
    private final KeywordServiceImpl keywordService;
    private final LibraryService libraryService;

    // TODO: 삭제될 레포지토리 의존성
    private final FeedRepository feedRepository;
    private final GenrePreferenceRepository genrePreferenceRepository;
    private final AvatarRepository avatarRepository;

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
                libraryService.getLibraryOrNull(user, novel),
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
                libraryAttractivePointService.getAttractivePoints(novel),
                libraryKeywordService.getKeywordNameAndCount(novel),
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

    @Transactional(readOnly = true)
    public PopularNovelsGetResponse getTodayPopularNovels() {
        List<Long> novelIdsFromPopularNovel = popularNovelService.getNovelIdsFromPopularNovel();
        List<Long> selectedNovelIdsFromPopularNovel = getSelectedNovelIdsFromPopularNovel(novelIdsFromPopularNovel);
        List<Novel> popularNovels = novelService.getSelectedPopularNovels(selectedNovelIdsFromPopularNovel);
        List<Feed> popularFeedsFromPopularNovels = feedRepository.findPopularFeedsByNovelIds(selectedNovelIdsFromPopularNovel);

        Map<Long, Feed> feedMap = createFeedMap(popularFeedsFromPopularNovels);
        Map<Byte, Avatar> avatarMap = createAvatarMap(feedMap);

        return PopularNovelsGetResponse.create(popularNovels, feedMap, avatarMap);
    }

    // TODO: DTO로 이전할 명분이 충분한 메서드
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

    private List<Long> getSelectedNovelIdsFromPopularNovel(List<Long> popularNovelIds) {
        Collections.shuffle(popularNovelIds);
        return popularNovelIds.size() > 10
                ? popularNovelIds.subList(0, 10)
                : popularNovelIds;
    }

    private Map<Long, Feed> createFeedMap(List<Feed> popularFeedsFromPopularNovels) {
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

}
