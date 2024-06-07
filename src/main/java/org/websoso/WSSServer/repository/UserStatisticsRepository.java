package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserStatistics;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {
    Optional<UserStatistics> findByUser(User user);

}
