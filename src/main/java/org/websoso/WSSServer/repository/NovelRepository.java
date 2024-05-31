package org.websoso.WSSServer.repository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {
}
