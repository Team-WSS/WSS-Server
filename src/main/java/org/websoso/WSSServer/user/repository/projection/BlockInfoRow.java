package org.websoso.WSSServer.user.repository.projection;

import org.websoso.WSSServer.dto.block.BlockGetResponse;

public record BlockInfoRow(
        Long blockId,
        Long userId,
        String nickname,
        String avatarImage
) {

    public BlockGetResponse toResponse() {
        return new BlockGetResponse(
                blockId,
                userId,
                nickname,
                avatarImage
        );
    }
}
