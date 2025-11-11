package org.websoso.WSSServer.novel.service;

import static org.websoso.WSSServer.exception.error.CustomKeywordError.KEYWORD_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.exception.exception.CustomKeywordException;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.repository.KeywordRepository;

@Service
@RequiredArgsConstructor
public class KeywordServiceImpl {
    private KeywordRepository keywordRepository;

    @Transactional(readOnly = true)
    public Keyword getKeywordOrException(Integer keywordId) {
        return keywordRepository.findById(keywordId)
                .orElseThrow(() -> new CustomKeywordException(KEYWORD_NOT_FOUND,
                        "keyword with the given id is not found"));
    }
}
