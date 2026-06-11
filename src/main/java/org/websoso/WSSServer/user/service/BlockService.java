package org.websoso.WSSServer.user.service;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.user.domain.Block;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.BlockRepository;

import static org.websoso.WSSServer.exception.error.CustomFeedError.BLOCKED_USER_ACCESS;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {

    private final BlockRepository blockRepository;

    public void createBlock(User blocker, User blocked) {
        try {
            Block block = Block.create(blocker.getUserId(), blocked.getUserId());
            blockRepository.save(block);
        } catch (DataIntegrityViolationException e) {
            return;
        }
    }

    public void unblock(Long blockId) {
        blockRepository.deleteById(blockId);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long blockingId, Long blockedId) {
        return blockRepository.existsByBlockingIdAndBlockedId(blockingId, blockedId);
    }

    @Transactional(readOnly = true)
    public void validateNotBlocked(Long userId, Long targetUserId) {

        if (userId.equals(targetUserId)) return;

        if (blockRepository.existsBlockRelation(userId, targetUserId)) {
            throw new CustomFeedException(BLOCKED_USER_ACCESS,
                    "cannot access this feed because either you or the feed author has blocked the other.");
        }
    }

    @Transactional(readOnly = true)
    public List<Block> findByBlockerId(Long blockingId) {
        return blockRepository.findByBlockingId(blockingId);
    }
}
