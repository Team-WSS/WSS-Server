package org.websoso.WSSServer.dto.userNovel;

import java.util.List;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.dto.Keyword.KeywordGetResponse;

public record UserNovelGetResponse(
        String novelTitle,
        String status,
        String startDate,
        String endDate,
        Float userNovelRating,
        List<String> attractivePoints,
        List<KeywordGetResponse> keywords
) {
    public static UserNovelGetResponse of(UserNovel userNovel, List<String> attractivePoints,
                                          List<KeywordGetResponse> keywords) {
        return new UserNovelGetResponse(
                userNovel.getNovel().getTitle(),
                userNovel.getStatus() != null ? userNovel.getStatus().getName() : null,
                userNovel.getStartDate() != null ? userNovel.getStartDate().toString() : null,
                userNovel.getEndDate() != null ? userNovel.getEndDate().toString() : null,
                userNovel.getUserNovelRating(),
                attractivePoints,
                keywords
        );
    }
}
