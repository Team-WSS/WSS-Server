package org.websoso.WSSServer.recentsearch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.websoso.WSSServer.recentsearch.domain.RecentSearch;

public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long>, RecentSearchNativeRepository,
        RecentSearchQueryDslRepository {

    /**
     * 단건 삭제 (ID 기준)
     */
    @Modifying(clearAutomatically = true)
    void deleteByIdAndUserId(Long id, Long userId);

    /**
     * 전체 삭제
     */
    @Modifying(clearAutomatically = true)
    void deleteByUserId(Long userId);

}
