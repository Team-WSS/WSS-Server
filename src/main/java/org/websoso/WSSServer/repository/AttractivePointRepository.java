package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.AttractivePoint;

@Repository
public interface AttractivePointRepository extends JpaRepository<AttractivePoint, Long> {
}
