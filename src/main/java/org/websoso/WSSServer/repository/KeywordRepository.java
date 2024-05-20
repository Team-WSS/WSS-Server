package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.websoso.WSSServer.domain.Keyword;

public interface KeywordRepository extends JpaRepository<Keyword, Integer> {
    List<Keyword> findByKeywordNameContaining(String query);
}