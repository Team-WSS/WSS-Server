package org.websoso.WSSServer.auth.validator;

import static org.websoso.WSSServer.exception.error.CustomBlockError.BLOCK_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomBlockError.INVALID_AUTHORIZED_BLOCK;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.user.domain.Block;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.exception.exception.CustomBlockException;
import org.websoso.WSSServer.user.repository.BlockRepository;

@Component
@RequiredArgsConstructor
public class BlockAuthorizationValidator implements ResourceAuthorizationValidator {

    private final BlockRepository blockRepository;

    @Override
    public boolean hasPermission(Long blockId, User user) {
        Block block = getBlockOrException(blockId);

        if (!isBlockOwner(block, user)) {
            throw new CustomBlockException(INVALID_AUTHORIZED_BLOCK,
                    "block with the given blockId is not from user with the given userId");
        }
        return true;
    }

    private Block getBlockOrException(Long blockId) {
        return blockRepository.findById(blockId)
                .orElseThrow(() -> new CustomBlockException(BLOCK_NOT_FOUND,
                        "block with the given blockId was not found"));
    }

    private boolean isBlockOwner(Block block, User user) {
        return block.getBlockingId().equals(user.getUserId());
    }

    @Override
    public Class<?> getResourceType() {
        return Block.class;
    }
}
