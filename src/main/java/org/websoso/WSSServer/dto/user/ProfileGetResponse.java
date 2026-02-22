package org.websoso.WSSServer.dto.user;

import java.util.List;
import java.util.stream.Collectors;
import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.user.domain.User;

public record ProfileGetResponse(
        String nickname,
        String intro,
        String avatarImage,
        Boolean isProfilePublic,
        List<String> genrePreferences
) {

    public static ProfileGetResponse of(boolean isMyProfile, User user, AvatarProfile avatar,
                                        List<GenrePreference> genrePreferences) {
        boolean isProfilePublic = isMyProfile
                ? true
                : user.getIsProfilePublic();
        return new ProfileGetResponse(
                user.getNickname(),
                user.getIntro(),
                avatar.getAvatarProfileImage(),
                isProfilePublic,
                genrePreferences.stream()
                        .map(gp -> gp.getGenre().getGenreName())
                        .collect(Collectors.toList()));
    }
}
