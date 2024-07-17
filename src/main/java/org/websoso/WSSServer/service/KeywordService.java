package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomKeywordCategoryError.KEYWORD_CATEGORY_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomKeywordError.KEYWORD_NOT_FOUND;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.KeywordCategory;
import org.websoso.WSSServer.domain.common.KeywordCategoryName;
import org.websoso.WSSServer.dto.keyword.CategoryGetResponse;
import org.websoso.WSSServer.dto.keyword.KeywordByCategoryGetResponse;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;
import org.websoso.WSSServer.exception.exception.CustomKeywordCategoryException;
import org.websoso.WSSServer.exception.exception.CustomKeywordException;
import org.websoso.WSSServer.repository.KeywordCategoryRepository;
import org.websoso.WSSServer.repository.KeywordRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final KeywordCategoryRepository keywordCategoryRepository;

    @Transactional(readOnly = true)
    public Keyword getKeywordOrException(Integer keywordId) {
        return keywordRepository.findById(keywordId)
                .orElseThrow(() -> new CustomKeywordException(KEYWORD_NOT_FOUND,
                        "keyword with the given id is not found"));
    }

    @Transactional(readOnly = true)
    public KeywordByCategoryGetResponse searchKeywordByCategory(String query) {
        List<CategoryGetResponse> categories = Arrays.stream(KeywordCategoryName.values())
                .map(category -> CategoryGetResponse.of(getKeywordCategory(category.getLabel()),
                        sortByCategory(category, searchKeyword(query))))
                .collect(Collectors.toList());
        return KeywordByCategoryGetResponse.of(categories);
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
                .map(KeywordGetResponse::of).collect(Collectors.toList());
    }

    private List<Keyword> searchKeyword(String query) {
        if (query == null || query.isBlank()) {
            return keywordRepository.findAll().stream().toList();
        }
        String[] words = query.split(" ");
        return keywordRepository.findAll().stream()
                .filter(keyword -> containsAllWords(keyword.getKeywordName(), words)).toList();
    }

    private boolean containsAllWords(String keywordName, String[] words) {
        for (String word : words) {
            if (!keywordName.contains(word)) {
                return false;
            }
        }
        return true;
    }

}
