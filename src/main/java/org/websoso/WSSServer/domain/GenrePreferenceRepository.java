package org.websoso.WSSServer.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenrePreferenceRepository extends JpaRepository<GenrePreference, Long> {

    List<GenrePreference> findByUser(User user);
}
