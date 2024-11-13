package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.userId = -1 WHERE c.userId = :userId")
    void updateUserToUnknown(Long userId);
}
