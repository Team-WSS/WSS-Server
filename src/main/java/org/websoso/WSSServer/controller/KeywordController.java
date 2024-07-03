package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.keyword.KeywordByCategoryGetResponse;
import org.websoso.WSSServer.service.KeywordService;

@RestController
@RequestMapping("/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    @GetMapping
    public ResponseEntity<KeywordByCategoryGetResponse> searchKeywordByCategory(
            @RequestParam(required = false) String query) {
        return ResponseEntity
                .status(OK)
                .body(keywordService.searchKeywordByCategory(query));
    }
}
