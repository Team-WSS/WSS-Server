package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Byte> {

    Optional<Genre> findByGenreName(String name);
}
