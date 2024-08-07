package org.websoso.WSSServer.dto.user;

import java.util.List;
import java.util.stream.Collectors;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.domain.User;

public record ProfileGetResponse(
        String nickname,
        String intro,
        String avatarImage,
        Boolean isProfilePublic,
        List<String> genrePreferences
) {

    public static ProfileGetResponse of(boolean isMyProfile, User user, Avatar avatar,
                                        List<GenrePreference> genrePreferences) {
        return new ProfileGetResponse(
                user.getNickname(),
                user.getIntro(),
                avatar.getAvatarImage(),
                isMyProfile ? true : user.getIsProfilePublic(),
                genrePreferences.stream()
                        .map(gp -> gp.getGenre().getGenreName())
                        .collect(Collectors.toList()));
    }
}
