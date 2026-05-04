package org.websoso.WSSServer.dto.recentsearch;

import java.time.LocalDateTime;

public record RecentSearchItem(Long id, String keyword, LocalDateTime searchedAt) {
}
