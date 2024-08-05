package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.websoso.WSSServer.domain.Genre;

public interface GenreRepository extends JpaRepository<Genre, Byte> {

    Optional<Genre> findByGenreName(String genreName);
}
