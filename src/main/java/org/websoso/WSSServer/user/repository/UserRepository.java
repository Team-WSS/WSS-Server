package org.websoso.WSSServer.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.user.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByNickname(String nickname);

    // TODO: Optional로 변경해야함
    User findBySocialId(String socialId);

    List<User> findAllByIsPushEnabledTrue();
}
