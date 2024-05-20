package org.websoso.WSSServer.repository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;

@Repository
public interface UserNovelRepository extends JpaRepository<UserNovel, Long> {
    Optional<UserNovel> findByNovelAndUser(Novel novel, User user);

    default UserNovel findUserNovelByNovelAndUserOrThrow(Novel novel, User user) {
        return findByNovelAndUser(novel, user).orElseThrow(() -> new EntityNotFoundException(""));
    }
}
