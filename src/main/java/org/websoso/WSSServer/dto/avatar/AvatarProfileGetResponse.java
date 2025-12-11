package org.websoso.WSSServer.dto.avatar;

import org.websoso.WSSServer.domain.AvatarProfile;
import org.websoso.WSSServer.domain.AvatarProfileLine;

public record AvatarProfileGetResponse(
        Long avatarProfileId,
        String avatarProfileName,
        String avatarProfileLine,
        String avatarProfileImage,
        String avatarCharacterImage,
        Boolean isRepresentative
) {
    public static AvatarProfileGetResponse of(AvatarProfile avatarProfile,
                                              AvatarProfileLine avatarProfileLine,
                                              Long representativeAvatarProfileId) {
        return new AvatarProfileGetResponse(
                avatarProfile.getAvatarProfileId(),
                avatarProfile.getAvatarProfileName(),
                avatarProfileLine.getAvatarLine(),
                avatarProfile.getAvatarProfileImage(),
                avatarProfile.getAvatarCharacterImage(),
                avatarProfile.isSameAvatarProfile(representativeAvatarProfileId)

        );
    }
}
