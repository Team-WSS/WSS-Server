package org.websoso.WSSServer.library.repository.cursor;

import java.time.LocalDateTime;
import java.time.LocalDate;

public record UserNovelCursor(
        Float lastRating,
        LocalDateTime lastCreatedDate,
        Long lastUserNovelId,
        Boolean rated,
        LocalDate lastStartDate
) {
}
