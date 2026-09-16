package org.websoso.WSSServer.collection.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.collection.domain.Collection;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long>, CollectionCustomRepository {
}
