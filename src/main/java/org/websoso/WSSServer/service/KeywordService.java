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

    public List<KeywordGetResponse> searchKeywordsByCategory(String query, KeywordCategory keywordCategory) {
        List<Keyword> searchedKeyword = keywordRepository.findByKeywordNameContaining(query);

        List<Keyword> filteredKeywords = searchedKeyword.stream()
                .filter(keyword -> keyword.getCategoryName().equals(keywordCategory))
                .toList();

        return filteredKeywords.stream()
                .map(KeywordGetResponse::of)
                .collect(Collectors.toList());
    }

    public List<CategoryGetResponse> getCategorys(String query) { //TODO 반복적인 코드 줄이기
        CategoryGetResponse worldview = CategoryGetResponse.of(KeywordCategory.WORLDVIEW,
                searchKeywordsByCategory(query, KeywordCategory.WORLDVIEW));
        CategoryGetResponse material = CategoryGetResponse.of(KeywordCategory.MATERIAL,
                searchKeywordsByCategory(query, KeywordCategory.MATERIAL));
        CategoryGetResponse character = CategoryGetResponse.of(KeywordCategory.CHARACTER,
                searchKeywordsByCategory(query, KeywordCategory.CHARACTER));
        CategoryGetResponse relationship = CategoryGetResponse.of(KeywordCategory.RELATIONSHIP,
                searchKeywordsByCategory(query, KeywordCategory.RELATIONSHIP));
        CategoryGetResponse vibe = CategoryGetResponse.of(KeywordCategory.VIBE,
                searchKeywordsByCategory(query, KeywordCategory.VIBE));
        return Arrays.asList(worldview, material, character, relationship, vibe);
    }

    public KeywordByCategoryGetResponse getAll(String query) { //TODO 함수 이름
        List<CategoryGetResponse> categorys = getCategorys(query);
        return KeywordByCategoryGetResponse.of(categorys);
    }
}
