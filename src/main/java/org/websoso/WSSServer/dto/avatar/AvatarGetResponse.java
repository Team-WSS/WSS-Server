package org.websoso.WSSServer.dto.avatar;

import java.util.List;
import java.util.Random;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.AvatarLine;

public record AvatarGetResponse(
        Byte avatarId,
        String avatarName,
        String avatarLine,
        String avatarImage,
        Boolean isRepresentative
) {
    public static AvatarGetResponse of(Avatar avatar, List<AvatarLine> avatarLines, Byte representativeAvatarId) {
        int avatarLineSize = avatarLines.size();
        int randomNumber = new Random().nextInt(avatarLineSize);
        return new AvatarGetResponse(
                avatar.getAvatarId(),
                avatar.getAvatarName(),
                avatarLines.get(randomNumber).getAvatarLine(),
                avatar.getAvatarImage(),
                avatar.getAvatarId().equals(representativeAvatarId)
        );
    }
}
