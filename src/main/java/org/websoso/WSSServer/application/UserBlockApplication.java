package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.domain.common.Role.ADMIN;
import static org.websoso.WSSServer.exception.error.CustomBlockError.ALREADY_BLOCKED;
import static org.websoso.WSSServer.exception.error.CustomBlockError.CANNOT_ADMIN_BLOCK;
import static org.websoso.WSSServer.exception.error.CustomBlockError.SELF_BLOCKED;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    @Transactional(readOnly = true)
    public BlocksGetResponse getBlockList(User user) {

        // 1. 차단 목록 조회
        List<Block> blocks = blockService.findByBlockerId(user.getUserId());

        // 2. 차단된 사람들의 ID 목록 추출
        List<Long> blockedUserIds = blocks.stream()
                .map(Block::getBlockedId)
                .toList();

        // 3. 유저 정보를 한 번에 조회
        List<User> blockedUsers = userService.findAllByIds(blockedUserIds);

        // 4. 유저 정보를 Map으로 변환
        Map<Long, User> userMap = blockedUsers.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        // 5. 아바타 프로필 ID 추출 및 조회
        List<Long> avatarProfileIds = blockedUsers.stream()
                .map(User::getAvatarProfileId)
                .distinct()
                .toList();

        Map<Long, AvatarProfile> avatarMap = avatarService.findAllByIds(avatarProfileIds).stream()
                .collect(Collectors.toMap(AvatarProfile::getAvatarProfileId, a -> a));

        // 6. 메모리 상에서 매핑
        List<BlockGetResponse> responses = blocks.stream()
                .map(block -> {
                    User blockedUser = userMap.get(block.getBlockedId());
                    AvatarProfile avatar = avatarMap.get(blockedUser.getAvatarProfileId());
                    return BlockGetResponse.of(block, blockedUser, avatar);
                })
                .toList();

        return new BlocksGetResponse(responses);
    }

//    public void deleteBlock(Long blockId) {
//        blockRepository.deleteById(blockId);
//    }
//
//    @Transactional(readOnly = true)
//    public boolean isBlocked(Long blockingId, Long blockedId) {
//        return blockRepository.existsByBlockingIdAndBlockedId(blockingId, blockedId);
//    }
}
