package org.websoso.WSSServer.novel.service;

import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.repository.GenreRepository;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public Genre getGenreOrException(String genreName) {
        return genreRepository.findByGenreName(genreName)
                .orElseThrow(() -> new CustomGenreException(GENRE_NOT_FOUND,
                        "genre with the given name is not found"));
    }

    @Transactional(readOnly = true)
    public List<Genre> getGenresOrException(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> uniqueNames = names.stream().distinct().toList();

        List<Genre> genres = genreRepository.findByNameIn(uniqueNames);

        if (genres.size() != uniqueNames.size()) {
            throw new CustomGenreException(GENRE_NOT_FOUND,
                    "genre with the given name is not found");
        }

        return genres;
    }
}
