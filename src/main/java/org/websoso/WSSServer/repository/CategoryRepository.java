package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
