package org.websoso.WSSServer.library.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.library.domain.KeywordCategory;

@Repository
public interface KeywordCategoryRepository extends JpaRepository<KeywordCategory, Byte> {

    Optional<KeywordCategory> findByKeywordCategoryName(String keywordCategoryName);

}
