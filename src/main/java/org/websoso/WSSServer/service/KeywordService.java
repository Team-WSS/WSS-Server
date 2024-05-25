package org.websoso.WSSServer.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.common.KeywordCategory;
import org.websoso.WSSServer.dto.Keyword.CategoryGetResponse;
import org.websoso.WSSServer.dto.Keyword.KeywordByCategoryGetResponse;
import org.websoso.WSSServer.dto.Keyword.KeywordGetResponse;
import org.websoso.WSSServer.repository.KeywordRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public KeywordByCategoryGetResponse searchKeywordByCategory(String query) {
        List<CategoryGetResponse> categories = Arrays.stream(KeywordCategory.values())
                .map(category -> CategoryGetResponse.of(category, sortByCategory(category, searchKeyword(query))))
                .collect(Collectors.toList());
        return KeywordByCategoryGetResponse.of(categories);
    }

    private List<Keyword> searchKeyword(String query) {
        if (query == null || query.isBlank()) {
            return keywordRepository.findAll().stream().toList();
        }
        String[] words = query.split(" ");
        return keywordRepository.findAll().stream()
                .filter(keyword -> containsAllKeywords(keyword.getKeywordName(), words)).toList();
    }

    private boolean containsAllKeywords(String keywordName, String[] words) {
        for (String word : words) {
            if (!keywordName.contains(word)) {
                return false;
            }
        }
        return true;
    }

    private List<KeywordGetResponse> sortByCategory(KeywordCategory keywordCategory, List<Keyword> searchedKeyword) {
        return searchedKeyword.stream().filter(keyword -> keyword.getCategoryName().equals(keywordCategory))
                .map(KeywordGetResponse::of).collect(Collectors.toList());
    }
}
