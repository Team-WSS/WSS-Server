package org.websoso.WSSServer.library.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.library.domain.AttractivePoint;

@Repository
public interface AttractivePointRepository extends JpaRepository<AttractivePoint, Long> {

    Optional<AttractivePoint> findByAttractivePointName(String name);
}
