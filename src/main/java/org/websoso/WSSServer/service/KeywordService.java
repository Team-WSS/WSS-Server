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

    private List<Keyword> searchKeyword(String query) {
        if (query == null || query.isEmpty()) {
            return keywordRepository.findAll().stream().toList();
        }
        return keywordRepository.findByKeywordNameContaining(query).stream().toList();
    }

    private List<KeywordGetResponse> sortByCategory(KeywordCategory keywordCategory, List<Keyword> searchedKeyword) {
        List<Keyword> filteredKeywords = searchedKeyword.stream()
                .filter(keyword -> keyword.getCategoryName().equals(keywordCategory))
                .toList();
        return filteredKeywords.stream()
                .map(KeywordGetResponse::of)
                .collect(Collectors.toList());
    }

    public KeywordByCategoryGetResponse searchKeywordByCategory(String query) {
        List<CategoryGetResponse> categorys = Arrays.stream(KeywordCategory.values())
                .map(category -> CategoryGetResponse.of(category, sortByCategory(category, searchKeyword(query))))
                .collect(Collectors.toList());
        return KeywordByCategoryGetResponse.of(categorys);
    }
}
