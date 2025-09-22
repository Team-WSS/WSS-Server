package org.websoso.support.version.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.support.version.domain.MinimumVersion;
import org.websoso.support.version.domain.OS;

@Repository
public interface MinimumVersionRepository extends JpaRepository<MinimumVersion, OS> {
}
