package org.websoso.WSSServer.dto.userNovel;

import java.util.List;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;

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
                userNovel.getStatus() == null ? null : userNovel.getStatus().getName(),
                userNovel.getStartDate() == null ? null : userNovel.getStartDate().toString(),
                userNovel.getEndDate() == null ? null : userNovel.getEndDate().toString(),
                userNovel.getUserNovelRating(),
                attractivePoints,
                keywords
        );
    }
}
