package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
