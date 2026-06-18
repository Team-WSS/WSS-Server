package org.websoso.WSSServer.library.repository.cursor;

import java.time.LocalDateTime;

public record UserNovelCursor(
        Float lastRating,
        LocalDateTime lastCreatedDate,
        Long lastUserNovelId,
        Boolean rated,
        String lastTitle
) {
}
