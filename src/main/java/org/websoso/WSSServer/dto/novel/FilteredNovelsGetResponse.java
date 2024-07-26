package org.websoso.WSSServer.dto.novel;

import java.util.List;

public record FilteredNovelsGetResponse(
        Long resultCount,
        Boolean isLoadable,
        List<NovelGetResponsePreview> novels
) {
    public static FilteredNovelsGetResponse of(Long resultCount, Boolean isLoadable,
                                               List<NovelGetResponsePreview> novels) {
        return new FilteredNovelsGetResponse(
                resultCount,
                isLoadable,
                novels
        );
    }
}
