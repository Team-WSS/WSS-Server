package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.websoso.WSSServer.domain.FeedImage;

public interface FeedImageRepository extends JpaRepository<FeedImage, Long> {
}
