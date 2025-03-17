package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Role.ADMIN;
import static org.websoso.WSSServer.exception.error.CustomBlockError.ALREADY_BLOCKED;
import static org.websoso.WSSServer.exception.error.CustomBlockError.CANNOT_ADMIN_BLOCK;
import static org.websoso.WSSServer.exception.error.CustomBlockError.SELF_BLOCKED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.Block;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.block.BlockGetResponse;
import org.websoso.WSSServer.dto.block.BlocksGetResponse;
import org.websoso.WSSServer.exception.exception.CustomBlockException;
import org.websoso.WSSServer.repository.BlockRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {

    private final UserService userService;
    private final AvatarService avatarService;
    private final BlockRepository blockRepository;

    public void block(User blocker, Long blockedId) {
        User blockedUser = userService.getUserOrException(blockedId);
        if (blockedUser.getRole() == ADMIN) {
            throw new CustomBlockException(CANNOT_ADMIN_BLOCK, "user requested to be blocked is ADMIN");
        }

        Long blockingId = blocker.getUserId();
        if (blockingId.equals(blockedId)) {
            throw new CustomBlockException(SELF_BLOCKED, "cannot block yourself");
        }

        if (blockRepository.existsByBlockingIdAndBlockedId(blockingId, blockedId)) {
            throw new CustomBlockException(ALREADY_BLOCKED, "account has already been blocked");
        }

        blockRepository.save(Block.create(blockingId, blockedId));
    }

    @Transactional(readOnly = true)
    public BlocksGetResponse getBlockList(User user) {
        List<Block> blocks = blockRepository.findByBlockingId(user.getUserId());
        List<BlockGetResponse> blockGetResponses = blocks.stream()
                .map(block -> {
                    User blockedUser = userService.getUserOrException(block.getBlockedId());
                    Avatar avatarOfBlockedUser = avatarService.getAvatarOrException(blockedUser.getAvatarId());
                    return BlockGetResponse.of(block, blockedUser, avatarOfBlockedUser);
                }).toList();
        return new BlocksGetResponse(blockGetResponses);
    }

    public void deleteBlock(Long blockId) {
        blockRepository.deleteById(blockId);
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(Long blockingId, Long blockedId) {
        return blockRepository.existsByBlockingIdAndBlockedId(blockingId, blockedId);
    }
}
