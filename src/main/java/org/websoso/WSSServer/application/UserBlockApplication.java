package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.domain.common.Role.ADMIN;
import static org.websoso.WSSServer.exception.error.CustomBlockError.ALREADY_BLOCKED;
import static org.websoso.WSSServer.exception.error.CustomBlockError.CANNOT_ADMIN_BLOCK;
import static org.websoso.WSSServer.exception.error.CustomBlockError.SELF_BLOCKED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.block.BlockGetResponse;
import org.websoso.WSSServer.dto.block.BlocksGetResponse;
import org.websoso.WSSServer.exception.exception.CustomBlockException;
import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.user.domain.Block;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.BlockRepository;
import org.websoso.WSSServer.user.service.AvatarService;
import org.websoso.WSSServer.user.service.BlockService;
import org.websoso.WSSServer.user.service.UserService;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBlockApplication {

    private final UserService userService;
    private final AvatarService avatarService;
    private final BlockService blockService;

    public void block(User blocker, Long blockedId) {

        // 1. 자기 자신을 차단하는지 검증
        if (blocker.isSameUserId(blockedId)) {
            throw new CustomBlockException(SELF_BLOCKED, "cannot block yourself");
        }

        // 2. 차단하는 대상이 운영자인지 검증
        User blockedUser = userService.getUserOrException(blockedId);
        if (blockedUser.isAdmin()) {
            throw new CustomBlockException(CANNOT_ADMIN_BLOCK, "user requested to be blocked is ADMIN");
        }

        // 3. 차단
        blockService.createBlock(blocker, blockedUser);
    }

//    @Transactional(readOnly = true)
//    public BlocksGetResponse getBlockList(User user) {
//        List<Block> blocks = blockRepository.findByBlockingId(user.getUserId());
//        List<BlockGetResponse> blockGetResponses = blocks.stream()
//                .map(block -> {
//                    User blockedUser = userService.getUserOrException(block.getBlockedId());
//                    AvatarProfile avatarOfBlockedUser = avatarService.getAvatarProfileOrException(blockedUser.getAvatarProfileId());
//                    return BlockGetResponse.of(block, blockedUser, avatarOfBlockedUser);
//                }).toList();
//        return new BlocksGetResponse(blockGetResponses);
//    }
//
//    public void deleteBlock(Long blockId) {
//        blockRepository.deleteById(blockId);
//    }
//
//    @Transactional(readOnly = true)
//    public boolean isBlocked(Long blockingId, Long blockedId) {
//        return blockRepository.existsByBlockingIdAndBlockedId(blockingId, blockedId);
//    }
}
