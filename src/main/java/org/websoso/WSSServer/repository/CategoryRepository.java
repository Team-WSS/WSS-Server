package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Category;
import org.websoso.WSSServer.domain.common.CategoryName;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Byte> {

    Optional<Category> findByCategoryName(CategoryName categoryName);
}
