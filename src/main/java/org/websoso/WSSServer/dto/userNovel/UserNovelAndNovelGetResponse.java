package org.websoso.WSSServer.dto.userNovel;

import java.util.List;

public record UserNovelAndNovelGetResponse(
        Long userNovelId,
        Long novelId,
        String title,
        String novelImage,
        Float novelRating,
        String readStatus,
        Boolean isInterest,
        Float userNovelRating,
        List<String> attractivePoints,
        String startDate,
        String endDate,
        List<String> keywords,
        List<String> myFeeds
) {
}
