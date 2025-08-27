package org.websoso.WSSServer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long>, NovelCustomRepository {

    @Query("SELECT n FROM Novel n JOIN UserNovel un ON n.novelId = un.novel.novelId " +
            "WHERE un.isDeleted = false " +
            "GROUP BY n.novelId " +
            "ORDER BY MAX(un.createdDate) DESC")
    Page<Novel> findSosoPick(Pageable pageable);

}
