package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserAppleToken;

@Repository
public interface UserAppleTokenRepository extends JpaRepository<UserAppleToken, Long> {

    Optional<UserAppleToken> findByUser(User user);
}
