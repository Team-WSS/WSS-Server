package org.websoso.WSSServer.application;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.common.KeywordCategoryName;
import org.websoso.WSSServer.dto.keyword.CategoryGetResponse;
import org.websoso.WSSServer.dto.keyword.KeywordByCategoryGetResponse;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;
import org.websoso.WSSServer.dto.keyword.KeywordPopularGetResponse;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.service.KeywordService;

@Service
@RequiredArgsConstructor
public class KeywordFindApplication {

    private final KeywordService keywordService;

    @Transactional(readOnly = true)
    public KeywordByCategoryGetResponse searchKeywordByCategory(String query) {
        List<Keyword> searchedKeywords = keywordService.searchKeyword(query);
        List<CategoryGetResponse> categories = Arrays.stream(KeywordCategoryName.values())
                .map(category -> CategoryGetResponse.of(
                        keywordService.getKeywordCategory(category.getLabel()),
                        sortByCategoryAndOrder(category, searchedKeywords)
                ))
                .toList();
        return KeywordByCategoryGetResponse.of(categories);
    }

    @Transactional(readOnly = true)
    public KeywordPopularGetResponse searchPopularKeyword(int size) {
        List<KeywordGetResponse> keywords = keywordService.getPopularKeywords(size).stream()
                .map(KeywordGetResponse::of)
                .toList();
        return KeywordPopularGetResponse.of(keywords);
    }

    private List<KeywordGetResponse> sortByCategoryAndOrder(KeywordCategoryName categoryName,
                                                            List<Keyword> searchedKeywords) {
        return searchedKeywords.stream()
                .filter(k -> k.getKeywordCategory().getKeywordCategoryName().equals(categoryName.getLabel()))
                .sorted(Comparator.comparing(Keyword::getSortOrder))
                .map(KeywordGetResponse::of)
                .toList();
    }
}
