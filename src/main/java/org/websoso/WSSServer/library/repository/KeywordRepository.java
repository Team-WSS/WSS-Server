package org.websoso.WSSServer.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.library.domain.Keyword;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Integer> {
}
