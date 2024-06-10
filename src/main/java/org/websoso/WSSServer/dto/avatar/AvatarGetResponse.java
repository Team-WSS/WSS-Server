package org.websoso.WSSServer.dto.avatar;

import java.util.List;
import java.util.Random;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.AvatarLine;
import org.websoso.WSSServer.domain.User;

public record AvatarGetResponse(
        Byte avatarId,
        String avatarName,
        String avatarLine,
        String avatarImage,
        Boolean isRepresentative
) {

    public static AvatarGetResponse of(Avatar avatar, List<AvatarLine> avatarLines, User user) {
        int avatarLineSize = avatarLines.size();
        int randomNumber = new Random().nextInt(avatarLineSize);
        Byte representativeAvatarId = user.getAvatarId();
        return new AvatarGetResponse(
                avatar.getAvatarId(),
                avatar.getAvatarName(),
                avatarLines.get(randomNumber).getAvatarLine(),
                avatar.getAvatarImage(),
                avatar.getAvatarId().equals(representativeAvatarId)
        );
    }
}
