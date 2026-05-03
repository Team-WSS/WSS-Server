package org.websoso.WSSServer.recentsearch.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.websoso.WSSServer.recentsearch.domain.RecentSearch;

public interface RecentSearchQueryDslRepository {

    /** 최신순 조회 */
    List<RecentSearch> findByUserIdOrderBySearchedAtDesc(long userId, int size, LocalDateTime lastSearchedAt,
                                                         Long lastRecentSearchId);

}
