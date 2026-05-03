package org.websoso.WSSServer.recentsearch.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.websoso.WSSServer.recentsearch.domain.RecentSearch;

public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long>, RecentSearchNativeRepository {

    /** 검색 이력 조회 */
    List<RecentSearch> findByUserId(Long userId, Pageable pageable);

    /** 단건 삭제 (ID 기준) */
    @Modifying(clearAutomatically = true)
    void deleteByIdAndUserId(Long id, Long userId);

    /** 전체 삭제 */
    @Modifying(clearAutomatically = true)
    void deleteByUserId(Long userId);

}
