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

    public List<KeywordGetResponse> searchKeywordsByCategory(String word, KeywordCategory keywordCategory) {
        List<Keyword> searchedKeyword = keywordRepository.findByKeywordNameContaining(word);

        List<Keyword> filteredKeywords = searchedKeyword.stream()
                .filter(keyword -> keyword.getCategoryName().equals(keywordCategory))
                .toList();

        return filteredKeywords.stream()
                .map(KeywordGetResponse::of)
                .collect(Collectors.toList());
    }

    public List<CategoryGetResponse> getCategorys(String word) { //TODO 반복적인 코드 줄이기
        CategoryGetResponse worldview = CategoryGetResponse.of(KeywordCategory.WORLDVIEW,
                searchKeywordsByCategory(word, KeywordCategory.WORLDVIEW));
        CategoryGetResponse material = CategoryGetResponse.of(KeywordCategory.MATERIAL,
                searchKeywordsByCategory(word, KeywordCategory.MATERIAL));
        CategoryGetResponse character = CategoryGetResponse.of(KeywordCategory.CHARACTER,
                searchKeywordsByCategory(word, KeywordCategory.CHARACTER));
        CategoryGetResponse relationship = CategoryGetResponse.of(KeywordCategory.RELATIONSHIP,
                searchKeywordsByCategory(word, KeywordCategory.RELATIONSHIP));
        CategoryGetResponse vibe = CategoryGetResponse.of(KeywordCategory.VIBE,
                searchKeywordsByCategory(word, KeywordCategory.VIBE));
        return Arrays.asList(worldview, material, character, relationship, vibe);
    }

    public KeywordByCategoryGetResponse getAll(String word) { //TODO 함수 이름
        List<CategoryGetResponse> categorys = getCategorys(word);
        return KeywordByCategoryGetResponse.of(categorys);
    }
}
