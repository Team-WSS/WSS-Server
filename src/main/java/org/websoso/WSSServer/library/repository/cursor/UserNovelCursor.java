package org.websoso.WSSServer.library.repository.cursor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 서재 정렬별 다음 페이지 조회 기준값을 저장한다.
 */
public record UserNovelCursor(
        Float lastRating,
        LocalDateTime lastCreatedDate,
        Long lastUserNovelId,
        Boolean rated,
        LocalDate lastReadDate,
        String lastTitle
) {
}
