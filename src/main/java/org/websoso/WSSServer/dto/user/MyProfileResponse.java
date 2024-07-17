package org.websoso.WSSServer.dto.user;

import java.util.List;
import java.util.stream.Collectors;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.domain.User;

public record MyProfileResponse(
        String nickname,
        String intro,
        String avatarImage,
        List<String> genrePreferences
) {

    public static MyProfileResponse of(User user, Avatar avatar, List<GenrePreference> genrePreferences) {
        return new MyProfileResponse(
                user.getNickname(),
                user.getIntro(),
                avatar.getAvatarImage(),
                genrePreferences.stream()
                        .map(gp -> gp.getGenre().getGenreName())
                        .collect(Collectors.toList()));
    }
}
