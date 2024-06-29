package org.websoso.WSSServer.dto.block;

import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.Block;
import org.websoso.WSSServer.domain.User;

public record BlockGetResponse(
        Long blockId,
        Long userId,
        String nickname,
        String avatarImage
) {
    public static BlockGetResponse of(Block block, User blockedUser, Avatar avatarOfBlockedUser) {
        return new BlockGetResponse(
                block.getBlockId(),
                blockedUser.getUserId(),
                blockedUser.getNickname(),
                avatarOfBlockedUser.getAvatarImage()
        );
    }
}
