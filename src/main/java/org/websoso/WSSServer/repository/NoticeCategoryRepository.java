package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.NoticeCategory;

@Repository
public interface NoticeCategoryRepository extends JpaRepository<NoticeCategory, Byte> {
}
