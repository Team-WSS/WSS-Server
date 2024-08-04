package org.websoso.WSSServer.dto.userNovel;

import java.util.List;
import org.websoso.WSSServer.domain.Novel;
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
    public static UserNovelGetResponse of(Novel novel, UserNovel userNovel, List<String> attractivePoints,
                                          List<KeywordGetResponse> keywords) {
        if (userNovel == null) {
            return new UserNovelGetResponse(
                    novel.getTitle(),
                    null,
                    null,
                    null,
                    0.0f,
                    attractivePoints,
                    keywords
            );
        }
        return new UserNovelGetResponse(
                novel.getTitle(),
                userNovel.getStatus() == null ? null : userNovel.getStatus().name(),
                userNovel.getStartDate() == null ? null : userNovel.getStartDate().toString(),
                userNovel.getEndDate() == null ? null : userNovel.getEndDate().toString(),
                userNovel.getUserNovelRating(),
                attractivePoints,
                keywords
        );
    }
}
