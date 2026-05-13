package org.websoso.WSSServer.library.service;

import static org.websoso.WSSServer.exception.error.CustomKeywordCategoryError.KEYWORD_CATEGORY_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomKeywordError.KEYWORD_NOT_FOUND;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.keyword.KeywordCountGetResponse;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.domain.KeywordCategory;
import org.websoso.WSSServer.exception.exception.CustomKeywordCategoryException;
import org.websoso.WSSServer.exception.exception.CustomKeywordException;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelKeyword;
import org.websoso.WSSServer.library.repository.KeywordCategoryRepository;
import org.websoso.WSSServer.library.repository.KeywordRepository;
import org.websoso.WSSServer.library.repository.UserNovelKeywordRepository;
import org.websoso.WSSServer.novel.domain.Novel;

@Service
@RequiredArgsConstructor
@Transactional
public class KeywordService {

    private static final int KEYWORD_SIZE = 5;

    private final KeywordRepository keywordRepository;
    private final KeywordCategoryRepository keywordCategoryRepository;
    private final UserNovelKeywordRepository userNovelKeywordRepository;

    @Transactional(readOnly = true)
    public Keyword getKeywordOrException(Integer keywordId) {
        return keywordRepository.findById(keywordId)
                .orElseThrow(() -> new CustomKeywordException(KEYWORD_NOT_FOUND,
                        "keyword with the given id is not found"));
    }

    public void createNovelKeyword(UserNovel userNovel, Keyword keyword) {
        userNovelKeywordRepository.save(UserNovelKeyword.create(userNovel, keyword));
    }

    public void createNovelKeywords(UserNovel userNovel, List<Integer> request) {
        for (Integer keywordId : request) {
            Keyword keyword = getKeywordOrException(keywordId);
            userNovelKeywordRepository.save(UserNovelKeyword.create(userNovel, keyword));
        }
    }

    public void deleteUserNovelKeywords(List<UserNovelKeyword> userNovelKeywords) {
        userNovelKeywordRepository.deleteAll(userNovelKeywords);
    }

    public KeywordCategory getKeywordCategory(String keywordCategoryName) {
        return keywordCategoryRepository.findByKeywordCategoryName(keywordCategoryName).orElseThrow(
                () -> new CustomKeywordCategoryException(KEYWORD_CATEGORY_NOT_FOUND,
                        "keyword category with the given name is not found"));
    }

    @Transactional(readOnly = true)
    public List<Keyword> getPopularKeywords(int size) {
        return userNovelKeywordRepository.findTopKeywordsByCount(PageRequest.of(0, size));
    }

    public List<Keyword> searchKeyword(String query) {
        List<Keyword> allKeywords = keywordRepository.findAllByOrderBySortOrderAsc();

        if (query == null || query.isBlank()) {
            return allKeywords;
        }

        String[] words = query.split(" ");
        return allKeywords.stream()
                .filter(keyword -> keyword.containsAllWords(words))
                .toList();
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

    public List<UserNovelKeyword> getKeywords(Novel novel) {
        return userNovelKeywordRepository.findAllByUserNovel_Novel(novel);
    }


}
