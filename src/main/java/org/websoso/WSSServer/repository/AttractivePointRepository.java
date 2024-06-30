package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.UserNovel;

@Repository
public interface AttractivePointRepository extends JpaRepository<AttractivePoint, Long> {
    Optional<AttractivePoint> findByUserNovel(UserNovel userNovel);
}
