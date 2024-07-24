package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.websoso.WSSServer.domain.RecentUserNovel;

public interface RecentUserNovelRepository extends JpaRepository<RecentUserNovel, Long> {

}
