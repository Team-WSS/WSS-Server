package org.websoso.WSSServer.dto.avatar;

import org.websoso.WSSServer.user.domain.Avatar;
import org.websoso.WSSServer.user.domain.AvatarLine;

public record AvatarGetResponse(
        Byte avatarId,
        String avatarName,
        String avatarLine,
        String avatarImage,
        Boolean isRepresentative
) {
    public static AvatarGetResponse of(Avatar avatar, AvatarLine avatarLine, Byte representativeAvatarId) {
        return new AvatarGetResponse(
                avatar.getAvatarId(),
                avatar.getAvatarName(),
                avatarLine.getAvatarLine(),
                avatar.getAvatarImage(),
                avatar.getAvatarId().equals(representativeAvatarId)
        );
    }
}
