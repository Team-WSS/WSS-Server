package org.websoso.WSSServer.novel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.novel.domain.PopularNovel;

@Repository
public interface PopularNovelRepository extends JpaRepository<PopularNovel, Long> {

}
