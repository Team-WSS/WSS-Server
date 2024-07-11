package org.websoso.WSSServer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Category;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.FeedCategory;

@Repository
public interface FeedCategoryRepository extends JpaRepository<FeedCategory, Long> {

    Optional<List<FeedCategory>> findByFeed(Feed feed);

    void deleteByCategory(Category category);
}
