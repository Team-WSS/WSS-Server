package org.websoso.WSSServer.dto.userNovel;

import java.util.List;

public record UserNovelAndNovelsGetResponse(
        Long userNovelCount,
        Boolean isLoadable,
        List<UserNovelAndNovelGetResponse> userNovels
) {

    public static UserNovelAndNovelsGetResponse of(Long userNovelCount, Boolean isLoadable,
                                                   List<UserNovelAndNovelGetResponse> userNovelAndNovelGetResponses) {
        return new UserNovelAndNovelsGetResponse(userNovelCount, isLoadable, userNovelAndNovelGetResponses);
    }
}
