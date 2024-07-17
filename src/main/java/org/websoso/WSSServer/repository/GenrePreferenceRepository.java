package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.domain.User;

@Repository
public interface GenrePreferenceRepository extends JpaRepository<GenrePreference, Long> {

    List<GenrePreference> findByUser(User user);
}
