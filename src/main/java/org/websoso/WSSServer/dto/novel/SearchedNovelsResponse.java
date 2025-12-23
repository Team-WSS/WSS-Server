package org.websoso.WSSServer.dto.novel;

import java.util.Collections;
import java.util.List;

public record SearchedNovelsResponse(
        long resultCount,
        boolean isLoadable,
        List<NovelSummaryResponse> novels
) {
    public static SearchedNovelsResponse of(List<NovelSummaryResponse> novels, long resultCount, boolean isLoadable) {
        return new SearchedNovelsResponse(
                resultCount,
                isLoadable,
                novels
        );
    }

    public static SearchedNovelsResponse empty() {
        return new SearchedNovelsResponse(
                0L,
                false,
                Collections.emptyList()
        );
    }
}
