package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;

public interface UserNovelCustomRepository {

    UserNovelCountGetResponse findUserNovelStatistics(Long userId);

    List<Long> findTodayPopularNovelsId(Pageable pageable);

    List<UserNovel> findUserNovelsByNoOffsetPagination(User owner, Long lastUserNovelId, int size,
                                                       String readStatus, String sortType);

    List<UserNovel> findByUserAndReadStatus(User owner, String readStatus);

    List<Novel> findTasteNovels(List<Genre> preferGenres);

    List<UserNovel> findFilteredUserNovels(Long userId, Boolean isInterest, List<String> readStatuses,
                                           List<String> attractivePoints, Float novelRating, String query,
                                           Long lastNovelId, int size, boolean isAscending);

    Long countByUserIdAndFilters(Long userId, Boolean isInterest, List<String> readStatuses,
                                 List<String> attractivePoints, Float novelRating, String query);
}
