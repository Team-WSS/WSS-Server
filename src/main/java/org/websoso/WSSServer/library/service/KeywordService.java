package org.websoso.WSSServer.library.service;

import static org.websoso.WSSServer.exception.error.CustomKeywordCategoryError.KEYWORD_CATEGORY_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomKeywordError.KEYWORD_NOT_FOUND;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.keyword.KeywordCountGetResponse;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.domain.KeywordCategory;
import org.websoso.WSSServer.domain.common.KeywordCategoryName;
import org.websoso.WSSServer.dto.keyword.CategoryGetResponse;
import org.websoso.WSSServer.dto.keyword.KeywordByCategoryGetResponse;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;
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

    @Transactional(readOnly = true)
    public KeywordByCategoryGetResponse searchKeywordByCategory(String query) {
        List<Keyword> searchedKeywords = searchKeyword(query);
        List<CategoryGetResponse> categories = Arrays.stream(KeywordCategoryName.values())
                .map(category -> CategoryGetResponse.of(getKeywordCategory(category.getLabel()),
                        sortByCategory(category, searchedKeywords)))
                .toList();
        return KeywordByCategoryGetResponse.of(categories);
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

    private KeywordCategory getKeywordCategory(String keywordCategoryName) {
        return keywordCategoryRepository.findByKeywordCategoryName(keywordCategoryName).orElseThrow(
                () -> new CustomKeywordCategoryException(KEYWORD_CATEGORY_NOT_FOUND,
                        "keyword category with the given name is not found"));
    }

    private List<KeywordGetResponse> sortByCategory(KeywordCategoryName keywordCategoryName,
                                                    List<Keyword> searchedKeyword) {
        return searchedKeyword.stream()
                .filter(keyword -> keyword.getKeywordCategory().getKeywordCategoryName()
                        .equals(keywordCategoryName.getLabel()))
                .map(KeywordGetResponse::of).toList();
    }

    private List<Keyword> searchKeyword(String query) {
        if (query == null || query.isBlank()) {
            return keywordRepository.findAll().stream().toList();
        }
        String[] words = query.split(" ");
        return keywordRepository.findAll().stream()
                .filter(keyword -> keyword.containsAllWords(words)).toList();
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
