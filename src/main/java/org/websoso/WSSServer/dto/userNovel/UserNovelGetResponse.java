package org.websoso.WSSServer.dto.userNovel;

import java.time.LocalDate;
import java.util.List;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;

public record UserNovelGetResponse(
        String novelTitle,
        String status,
        LocalDate startDate,
        LocalDate endDate,
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
                userNovel.getStartDate(),
                userNovel.getEndDate(),
                userNovel.getUserNovelRating(),
                attractivePoints,
                keywords
        );
    }
}
