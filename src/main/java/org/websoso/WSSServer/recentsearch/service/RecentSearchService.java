package org.websoso.WSSServer.recentsearch.service;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.recentsearch.RecentSearchItem;
import org.websoso.WSSServer.dto.recentsearch.RecentSearchesGetResponse;
import org.websoso.WSSServer.recentsearch.domain.RecentSearch;
import org.websoso.WSSServer.recentsearch.repository.RecentSearchRepository;

@Service
@RequiredArgsConstructor
public class RecentSearchService {

    private static final int MAX_SIZE = 200;
    private static final int MAX_KEYWORD_LENGTH = 100;

    private final RecentSearchRepository repository;

    @Transactional(propagation = REQUIRES_NEW)
    public void add(long userId, String keyword) {
        if (keyword == null) {
            return;
        }

        String trimmed = keyword.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        if (trimmed.length() > MAX_KEYWORD_LENGTH) {
            trimmed = trimmed.substring(0, MAX_KEYWORD_LENGTH);
        }

        repository.upsert(userId, trimmed, LocalDateTime.now());
        repository.trimToMaxSize(userId, MAX_SIZE);
    }

    @Transactional(readOnly = true)
    public RecentSearchesGetResponse findRecentSearchesWithCursor(long userId, int size, LocalDateTime lastSearchedAt,
                                                                Long lastRecentSearchId) {
        List<RecentSearch> rows = repository.findByUserIdOrderBySearchedAtDesc(userId, size + 1, lastSearchedAt,
                lastRecentSearchId);

        boolean hasNext = rows.size() > size;

        List<RecentSearchItem> items = rows.stream()
                .limit(size)
                .map(it -> new RecentSearchItem(it.getId(), it.getKeyword(), it.getSearchedAt()))
                .toList();

        return new RecentSearchesGetResponse(items, hasNext);
    }

    @Transactional
    public void remove(long userId, long id) {
        repository.deleteByIdAndUserId(id, userId);
    }

    @Transactional
    public void clear(long userId) {
        repository.deleteByUserId(userId);
    }

    @Transactional
    public int deleteBatch(LocalDateTime threshold, int batchSize) {
        return repository.deleteOlderThan(threshold, batchSize);
    }
}