package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Notice;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

}
