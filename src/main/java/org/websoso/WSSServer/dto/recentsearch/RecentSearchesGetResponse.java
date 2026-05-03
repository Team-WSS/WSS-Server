package org.websoso.WSSServer.dto.recentsearch;

import java.util.List;

public record RecentSearchesGetResponse(
        List<RecentSearchItem> recentSearches,
        boolean isLoadable
) {
}
