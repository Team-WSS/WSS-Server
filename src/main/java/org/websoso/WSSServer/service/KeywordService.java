package org.websoso.WSSServer.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public List<KeywordGetResponse> getKeywordsByCategory(KeywordCategory keywordCategory) {
        return keywordRepository.findAllByCategoryName(keywordCategory)
                .stream()
                .map(KeywordGetResponse::of)
                .collect(Collectors.toList());
    }

    public List<CategoryGetResponse> getCategorys() { //TODO 반복적인 코드 줄이기
        CategoryGetResponse worldview = CategoryGetResponse.of(KeywordCategory.WORLDVIEW,
                getKeywordsByCategory(KeywordCategory.WORLDVIEW));
        CategoryGetResponse material = CategoryGetResponse.of(KeywordCategory.MATERIAL,
                getKeywordsByCategory(KeywordCategory.MATERIAL));
        CategoryGetResponse character = CategoryGetResponse.of(KeywordCategory.CHARACTER,
                getKeywordsByCategory(KeywordCategory.CHARACTER));
        CategoryGetResponse relationship = CategoryGetResponse.of(KeywordCategory.RELATIONSHIP,
                getKeywordsByCategory(KeywordCategory.RELATIONSHIP));
        CategoryGetResponse vibe = CategoryGetResponse.of(KeywordCategory.VIBE,
                getKeywordsByCategory(KeywordCategory.VIBE));
        return Arrays.asList(worldview, material, character, relationship, vibe);
    }

    public KeywordByCategoryGetResponse getAll() { //TODO 함수 이름
        List<CategoryGetResponse> categorys = getCategorys();
        return KeywordByCategoryGetResponse.of(categorys);
    }
}
