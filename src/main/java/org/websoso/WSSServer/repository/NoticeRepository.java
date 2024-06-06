package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Notice;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByUserId(Long userId);
}
