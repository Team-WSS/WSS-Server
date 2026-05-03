package org.websoso.WSSServer.controller;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.recentsearch.RecentSearchItem;
import org.websoso.WSSServer.dto.recentsearch.RecentSearchesGetResponse;
import org.websoso.WSSServer.recentsearch.service.RecentSearchService;
import org.websoso.WSSServer.user.domain.User;

@RestController
@RequestMapping("/novels/recent-searches")
@RequiredArgsConstructor
public class RecentSearchController {

    private final RecentSearchService service;

    @GetMapping
    public ResponseEntity<RecentSearchesGetResponse> findRecentSearches(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastSearchedAt,
            @RequestParam(required = false) Long lastRecentSearchId) {
        return ResponseEntity.ok(service.findRecentSearchesWithCursor(user.getUserId(), size, lastSearchedAt, lastRecentSearchId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal User user,
                                       @PathVariable long id) {
        service.remove(user.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@AuthenticationPrincipal User user) {
        service.clear(user.getUserId());
        return ResponseEntity.noContent().build();
    }

}
