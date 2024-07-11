package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.FeedCategory;

@Repository
public interface FeedCategoryRepository extends JpaRepository<FeedCategory, Long> {

}
