package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.KeywordCategory;

@Repository
public interface KeywordCategoryRepository extends JpaRepository<KeywordCategory, Byte> {

    Optional<KeywordCategory> findByKeywordCategoryName(String keywordCategoryName);

}
