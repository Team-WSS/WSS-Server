package org.websoso.WSSServer.dto.user;

import java.util.List;

public record MyProfileResponse(
        String nickname,
        String intro,
        String avatarImage,
        List<String> genrePreferences
) {
}
