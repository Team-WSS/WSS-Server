package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.UserAppleToken;

@Repository
public interface UserAppleTokenRepository extends JpaRepository<UserAppleToken, Long> {
}
