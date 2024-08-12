package org.websoso.WSSServer.dto.userNovel;

import java.util.List;
import org.websoso.WSSServer.domain.Genre;

public record UserGenrePreferencesGetResponse(
        List<UserGenrePreferenceGetResponse> genrePreferences
) {

    public static UserGenrePreferencesGetResponse of(
            List<UserGenrePreferenceGetResponse> userGenrePreferenceGetResponses) {
        return new UserGenrePreferencesGetResponse(userGenrePreferenceGetResponses);
    }
}
