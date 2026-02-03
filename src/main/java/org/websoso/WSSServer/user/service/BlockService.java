package org.websoso.WSSServer.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.user.domain.Block;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.BlockRepository;

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
    public List<Block> findByBlockerId(Long blockingId) {
        return blockRepository.findByBlockingId(blockingId);
    }
}
