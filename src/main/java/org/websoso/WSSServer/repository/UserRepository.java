package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByNickname(String nickname);

    User findBySocialId(String socialId);

    Optional<User> findBySocialIdAndEmail(String socialId, String email);
}
