package org.websoso.WSSServer.dto.novel;

import java.util.List;

public record SearchedNovelsGetResponse(
        Long resultCount,
        Boolean isLoadable,
        List<NovelGetResponsePreview> novels
) {
    public static SearchedNovelsGetResponse of(Long resultCount, Boolean isLoadable,
                                               List<NovelGetResponsePreview> novels) {
        return new SearchedNovelsGetResponse(
                resultCount,
                isLoadable,
                novels
        );
    }
}
