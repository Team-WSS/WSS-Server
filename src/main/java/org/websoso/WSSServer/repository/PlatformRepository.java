package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.Platform;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {

    List<Platform> findAllByNovel(Novel novel);
}
