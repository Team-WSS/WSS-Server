package org.websoso.WSSServer.dto.userNovel;

import java.util.List;

public record UserNovelsV2GetResponse(
        Long userNovelCount,
        Boolean isLoadable,
        String nextCursor,
        List<UserNovelAndNovelGetResponse> userNovels
) {
}
