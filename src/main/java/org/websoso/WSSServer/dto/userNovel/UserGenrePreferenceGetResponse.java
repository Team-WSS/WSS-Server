package org.websoso.WSSServer.dto.userNovel;

public record UserGenrePreferenceGetResponse(
        String genreName,
        String genreImage,
        Long genreCount
) {
}
