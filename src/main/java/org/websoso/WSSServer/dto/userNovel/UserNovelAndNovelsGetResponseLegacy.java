package org.websoso.WSSServer.dto.userNovel;

import java.util.List;

public record UserNovelAndNovelsGetResponseLegacy(
        Long userNovelCount,
        Boolean isLoadable,
        List<UserNovelAndNovelGetResponseLegacy> userNovels
) {
    public static UserNovelAndNovelsGetResponseLegacy of(Long userNovelCount, Boolean isLoadable,
                                                         List<UserNovelAndNovelGetResponseLegacy> userNovelAndNovelGetResponseLegacies) {
        return new UserNovelAndNovelsGetResponseLegacy(userNovelCount, isLoadable,
                userNovelAndNovelGetResponseLegacies);
    }
}
