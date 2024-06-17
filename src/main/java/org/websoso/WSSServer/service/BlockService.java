package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Role.ADMIN;
import static org.websoso.WSSServer.exception.block.BlockErrorCode.ALREADY_BLOCKED;
import static org.websoso.WSSServer.exception.block.BlockErrorCode.BLOCK_NOT_FOUND;
import static org.websoso.WSSServer.exception.block.BlockErrorCode.CANNOT_ADMIN_BLOCK;
import static org.websoso.WSSServer.exception.block.BlockErrorCode.INVALID_AUTHORIZED_BLOCK;
import static org.websoso.WSSServer.exception.block.BlockErrorCode.SELF_BLOCKED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.Block;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.Role;
import org.websoso.WSSServer.dto.block.BlockGetResponse;
import org.websoso.WSSServer.dto.block.BlocksGetResponse;
import org.websoso.WSSServer.exception.block.exception.AdminBlockRestrictionException;
import org.websoso.WSSServer.exception.block.exception.AlreadyBlockedException;
import org.websoso.WSSServer.exception.block.exception.BlockNotFoundException;
import org.websoso.WSSServer.exception.block.exception.InvalidAuthorizedBlockException;
import org.websoso.WSSServer.exception.block.exception.SelfBlockedException;
import org.websoso.WSSServer.repository.BlockRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

    private final UserService userService;
    private final AvatarService avatarService;
    private final BlockRepository blockRepository;

    private static final Role ADMIN_ROLE = ADMIN;

    @Transactional
    public void block(User blocker, Long blockedId) {
        User blockedUser = userService.getUserOrException(blockedId);
        if (blockedUser.getRole() == ADMIN_ROLE) {
            throw new AdminBlockRestrictionException(CANNOT_ADMIN_BLOCK, "user requested to be blocked is ADMIN");
        }

        Long blockingId = blocker.getUserId();
        if (blockingId.equals(blockedId)) {
            throw new SelfBlockedException(SELF_BLOCKED, "cannot block yourself");
        }

        if (blockRepository.existsByBlockingIdAndBlockedId(blockingId, blockedId)) {
            throw new AlreadyBlockedException(ALREADY_BLOCKED, "account has already been blocked");
        }

        blockRepository.save(Block.create(blockingId, blockedId));
    }

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

    @Transactional
    public void deleteBlock(User user, Long blockId) {
        Block block = blockRepository.findById(blockId).orElseThrow(() ->
                new BlockNotFoundException(BLOCK_NOT_FOUND, "block with the given blockId was not found"));
        if (!block.getBlockingId().equals(user.getUserId())) {
            throw new InvalidAuthorizedBlockException(INVALID_AUTHORIZED_BLOCK,
                    "block with the given blockId is not from user with the given userId");
        }
        blockRepository.delete(block);
    }
}
