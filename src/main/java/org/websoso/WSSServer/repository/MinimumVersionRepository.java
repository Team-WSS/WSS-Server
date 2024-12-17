package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.MinimumVersion;
import org.websoso.WSSServer.domain.common.OS;

@Repository
public interface MinimumVersionRepository extends JpaRepository<MinimumVersion, OS> {
}
