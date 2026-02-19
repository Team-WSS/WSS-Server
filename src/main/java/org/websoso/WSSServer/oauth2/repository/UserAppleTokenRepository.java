package org.websoso.WSSServer.oauth2.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.oauth2.domain.UserAppleToken;

@Repository
public interface UserAppleTokenRepository extends JpaRepository<UserAppleToken, Long> {

    Optional<UserAppleToken> findByUser(User user);
}
