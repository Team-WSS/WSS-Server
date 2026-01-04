package org.websoso.WSSServer.dto.novel;

import java.util.List;

public record FilteredNovelsResponse(
        long resultCount,
        boolean isLoadable,
        List<NovelSummaryResponse> novels
) {
    public static FilteredNovelsResponse of(List<NovelSummaryResponse> novels, long resultCount, boolean isLoadable) {
        return new FilteredNovelsResponse(
                resultCount,
                isLoadable,
                novels
        );
    }
}
