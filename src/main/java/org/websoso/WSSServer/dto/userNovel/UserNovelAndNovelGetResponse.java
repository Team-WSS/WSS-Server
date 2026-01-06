package org.websoso.WSSServer.dto.userNovel;

import java.time.LocalDate;
import java.util.List;
import org.websoso.WSSServer.library.domain.AttractivePoint;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelAttractivePoint;
import org.websoso.WSSServer.library.domain.UserNovelKeyword;

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
        LocalDate startDate,
        LocalDate endDate,
        List<String> keywords,
        List<String> myFeeds
) {
    public static UserNovelAndNovelGetResponse from(UserNovel userNovel, Float novelRatingAvg, List<String> feeds) {
        Novel novel = userNovel.getNovel();

        List<String> attractivePoints = userNovel.getUserNovelAttractivePoints().stream()
                .map(UserNovelAttractivePoint::getAttractivePoint)
                .map(AttractivePoint::getAttractivePointName)
                .toList();

        List<String> keywords = userNovel.getUserNovelKeywords().stream()
                .map(UserNovelKeyword::getKeyword)
                .map(Keyword::getKeywordName)
                .toList();

        return new UserNovelAndNovelGetResponse(
                userNovel.getUserNovelId(),
                novel.getNovelId(),
                novel.getTitle(),
                novel.getNovelImage(),
                novelRatingAvg,
                userNovel.getStatus() != null
                        ? userNovel.getStatus().name()
                        : null,
                userNovel.getIsInterest(),
                userNovel.getUserNovelRating(),
                attractivePoints,
                userNovel.getStartDate(),
                userNovel.getEndDate(),
                keywords,
                feeds
        );
    }
}
