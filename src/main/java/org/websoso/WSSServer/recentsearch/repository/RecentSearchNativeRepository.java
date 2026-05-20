package org.websoso.WSSServer.recentsearch.repository;

import java.time.LocalDateTime;

public interface RecentSearchNativeRepository {

    /** (user_id, keyword) 기준 upsert. 있으면 searched_at 갱신, 없으면 삽입 */
    void upsert(Long userId, String keyword, LocalDateTime searchedAt);

    /** 사용자별 최신 maxSize 개만 남기고 나머지 삭제. 삭제된 행 수 반환 */
    void trimToMaxSize(Long userId, int maxSize);

    /** threshold 이전 데이터 batchSize 만큼 삭제 (만료 배치용) */
    int deleteOlderThan(LocalDateTime threshold, int batchSize);

}
