package org.websoso.WSSServer.library.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.common.UserNovelSortType;
import org.websoso.WSSServer.library.repository.cursor.UserNovelCursor;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;

public interface UserNovelCustomRepository {

    UserNovelCountGetResponse findUserNovelStatistics(Long userId);

    List<Long> findTodayPopularNovelsId(Pageable pageable);

    List<Novel> findTasteNovels(List<Genre> preferGenres);

    List<UserNovel> findFilteredUserNovels(Long userId, Boolean isInterest, List<String> readStatuses,
                                           List<String> attractivePoints, Float novelRating, String query,
                                           Long lastNovelId, int size, boolean isAscending, LocalDateTime updatedSince);

    Long countByUserIdAndFilters(Long userId, Boolean isInterest, List<String> readStatuses,
                                 List<String> attractivePoints, Float novelRating, String query,
                                 LocalDateTime updatedSince);

    List<UserNovel> findFilteredUserNovelsV2(Long userId, Boolean isInterest, List<String> readStatuses,
                                             List<String> genres, Boolean isCompleted, Float ratingMin,
                                             Float ratingMax, Boolean unratedOnly, List<String> attractivePoints,
                                             List<String> keywords, UserNovelCursor cursor, int size,
                                             UserNovelSortType sortType);

    Long countByUserIdAndFiltersV2(Long userId, Boolean isInterest, List<String> readStatuses, List<String> genres,
                                   Boolean isCompleted, Float ratingMin, Float ratingMax, Boolean unratedOnly,
                                   List<String> attractivePoints, List<String> keywords);
}
