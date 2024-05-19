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

    public List<Keyword> searchKeyword(String query) {
        List<Keyword> searchedKeyword;

        if (query == null || query.isEmpty()) {
            searchedKeyword = keywordRepository.findAll();
        } else {
            searchedKeyword = keywordRepository.findByKeywordNameContaining(query);
        }

        return searchedKeyword.stream()
                .toList();
    }

    public List<KeywordGetResponse> sortByCategory(KeywordCategory keywordCategory, List<Keyword> searchedKeyword) {
        List<Keyword> filteredKeywords = searchedKeyword.stream()
                .filter(keyword -> keyword.getCategoryName().equals(keywordCategory))
                .toList();

        return filteredKeywords.stream()
                .map(KeywordGetResponse::of)
                .collect(Collectors.toList());
    }

    public List<CategoryGetResponse> getResultByCategory(String query) { //TODO 반복적인 코드 줄이기
        CategoryGetResponse worldview = CategoryGetResponse.of(KeywordCategory.WORLDVIEW,
                sortByCategory(KeywordCategory.WORLDVIEW, searchKeyword(query)));
        CategoryGetResponse material = CategoryGetResponse.of(KeywordCategory.MATERIAL,
                sortByCategory(KeywordCategory.MATERIAL, searchKeyword(query)));
        CategoryGetResponse character = CategoryGetResponse.of(KeywordCategory.CHARACTER,
                sortByCategory(KeywordCategory.CHARACTER, searchKeyword(query)));
        CategoryGetResponse relationship = CategoryGetResponse.of(KeywordCategory.RELATIONSHIP,
                sortByCategory(KeywordCategory.RELATIONSHIP, searchKeyword(query)));
        CategoryGetResponse vibe = CategoryGetResponse.of(KeywordCategory.VIBE,
                sortByCategory(KeywordCategory.VIBE, searchKeyword(query)));
        return Arrays.asList(worldview, material, character, relationship, vibe);
    }

    public KeywordByCategoryGetResponse searchKeywordByCategory(String query) { //TODO 함수 이름
        List<CategoryGetResponse> categorys = getResultByCategory(query);
        return KeywordByCategoryGetResponse.of(categorys);
    }
}
