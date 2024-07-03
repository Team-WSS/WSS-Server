package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.keyword.KeywordErrorCode.KEYWORD_NOT_FOUND;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.common.KeywordCategory;
import org.websoso.WSSServer.dto.keyword.CategoryGetResponse;
import org.websoso.WSSServer.dto.keyword.KeywordByCategoryGetResponse;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;
import org.websoso.WSSServer.exception.keyword.exception.InvalidKeywordException;
import org.websoso.WSSServer.repository.KeywordRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class KeywordService {

    private final KeywordRepository keywordRepository;

    @Transactional(readOnly = true)
    public Keyword getKeywordOrException(Integer keywordId) {
        return keywordRepository.findById(keywordId)
                .orElseThrow(() -> new InvalidKeywordException(KEYWORD_NOT_FOUND,
                        "keyword with the given id is not found"));
    }

    @Transactional(readOnly = true)
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

    private List<KeywordGetResponse> sortByCategory(KeywordCategory keywordCategory, List<Keyword> searchedKeyword) {
        return searchedKeyword.stream().filter(keyword -> keyword.getCategoryName().equals(keywordCategory))
                .map(KeywordGetResponse::of).collect(Collectors.toList());
    }
}
