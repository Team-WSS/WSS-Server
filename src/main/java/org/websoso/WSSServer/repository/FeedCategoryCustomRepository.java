package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.websoso.WSSServer.domain.Category;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Genre;

public interface FeedCategoryCustomRepository {

    Slice<Feed> findRecommendedFeedsByCategoryLabel(Category category, Long lastFeedId, Long userId,
                                                    PageRequest pageRequest, List<Genre> genres);
}
