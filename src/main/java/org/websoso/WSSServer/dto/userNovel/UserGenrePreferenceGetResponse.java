package org.websoso.WSSServer.dto.userNovel;

import org.websoso.WSSServer.domain.Genre;

public record UserGenrePreferenceGetResponse(
        String genreName,
        String genreImage,
        Long genreCount
) {

    public static UserGenrePreferenceGetResponse of(Genre genre, Long genreCount) {
        return new UserGenrePreferenceGetResponse(
                genre.getGenreName(),
                genre.getGenreImage(),
                genreCount
        );
    }
}
