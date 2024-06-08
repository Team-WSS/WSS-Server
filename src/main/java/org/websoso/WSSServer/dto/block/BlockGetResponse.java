package org.websoso.WSSServer.dto.block;

public record BlockGetResponse(
        Long blockId,
        Long userId,
        String nickname,
        String avatarImage
) {
}
