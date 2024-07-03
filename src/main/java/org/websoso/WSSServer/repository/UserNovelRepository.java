package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;

@Repository
public interface UserNovelRepository extends JpaRepository<UserNovel, Long> {

    Optional<UserNovel> findByNovelAndUser(Novel novel, User user);

}
