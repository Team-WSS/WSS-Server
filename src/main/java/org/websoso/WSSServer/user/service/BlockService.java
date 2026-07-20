package org.websoso.WSSServer.user.service;

import static org.websoso.WSSServer.user.exception.CustomBlockError.ALREADY_BLOCKED;
import static org.websoso.WSSServer.user.exception.CustomBlockError.BLOCKED_USER_ACCESS;
import static org.websoso.WSSServer.user.exception.CustomBlockError.BLOCK_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.feed.exception.CustomFeedException;
import org.websoso.WSSServer.user.domain.Block;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.CustomBlockException;
import org.websoso.WSSServer.user.exception.DuplicateBlockException;
import org.websoso.WSSServer.user.repository.BlockConstraintViolationDetector;
import org.websoso.WSSServer.user.repository.BlockRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {

    private final BlockRepository blockRepository;
    private final BlockConstraintViolationDetector constraintViolationDetector;

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

    public void unblock(Block block) {
        blockRepository.delete(block);
    }

    @Transactional(readOnly = true)
    public Block getBlockOrException(Long blockId) {
        return blockRepository.findById(blockId)
                .orElseThrow(() -> new CustomBlockException(
                        BLOCK_NOT_FOUND,
                        "block with the given blockId was not found"
                ));
    }

    @Transactional(readOnly = true)
    public boolean hasBlockRelation(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return false;
        }

        return blockRepository.existsBlockRelation(userId, targetUserId);
    }

    @Transactional(readOnly = true)
    public void validateNotBlocked(Long userId, Long targetUserId) {

        if (hasBlockRelation(userId, targetUserId)) {
            throw new CustomBlockException(
                    BLOCKED_USER_ACCESS,
                    "cannot access content because either user has blocked the other"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<Block> findByBlockerId(Long blockingId) {
        return blockRepository.findByBlockingId(blockingId);
    }

    @Transactional(readOnly = true)
    public List<Long> findBlockRelationUserIds(Long userId) {
        return blockRepository.findBlockRelationUserIds(userId);
    }
}
