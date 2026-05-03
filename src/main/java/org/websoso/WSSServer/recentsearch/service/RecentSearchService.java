package org.websoso.WSSServer.recentsearch.service;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.recentsearch.RecentSearchItem;
import org.websoso.WSSServer.recentsearch.domain.RecentSearch;
import org.websoso.WSSServer.recentsearch.repository.RecentSearchRepository;

@Service
@RequiredArgsConstructor
public class RecentSearchService {

    private static final int MAX_SIZE = 20;
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
    public List<RecentSearchItem> findRecentSearches(long userId) {

        PageRequest pageRequest = PageRequest.of(0, MAX_SIZE, Sort.by(DESC, "searchedAt"));

        List<RecentSearch> rows = repository.findByUserId(userId, pageRequest);

        return rows.stream()
                .map(it -> new RecentSearchItem(it.getId(), it.getKeyword(), it.getSearchedAt()))
                .toList();
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