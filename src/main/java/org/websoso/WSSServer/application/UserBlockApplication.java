package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.user.exception.CustomBlockError.CANNOT_ADMIN_BLOCK;
import static org.websoso.WSSServer.user.exception.CustomBlockError.SELF_BLOCKED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.block.BlocksGetResponse;
import org.websoso.WSSServer.user.domain.Block;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.CustomBlockException;
import org.websoso.WSSServer.user.service.BlockQueryService;
import org.websoso.WSSServer.user.service.BlockService;
import org.websoso.WSSServer.user.service.UserService;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBlockApplication {

    private final UserService userService;
    private final BlockService blockService;
    private final BlockQueryService blockQueryService;

    @Deprecated(since = "PUT /blocks/users/{blockedUserId}/v2로 완벽 교체시")
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

    public void blockV2(User blocker, Long blockedId) {

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
        blockService.createBlockV2(blocker, blockedUser);
    }

    public void deleteBlock(User user, Long blockId) {
        Block block = blockService.getBlockOrException(blockId);
        block.validateOwner(user.getUserId());
        blockService.unblock(block);
    }

    @Transactional(readOnly = true)
    public BlocksGetResponse getBlockList(User user) {
        return BlocksGetResponse.of(blockQueryService.findBlockRows(user.getUserId()));
    }

}
