package org.websoso.WSSServer.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.dto.novel.FilteredNovelsGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponsePreview;
import org.websoso.WSSServer.dto.novel.SearchedNovelsGetResponse;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.GenreServiceImpl;
import org.websoso.WSSServer.novel.service.KeywordServiceImpl;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;

@Service
@RequiredArgsConstructor
public class SearchNovelApplication {

    private final NovelServiceImpl novelService;
    private final GenreServiceImpl genreService;
    private final KeywordServiceImpl keywordService;

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

}
