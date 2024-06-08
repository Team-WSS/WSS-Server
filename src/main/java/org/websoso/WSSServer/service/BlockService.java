package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.block.BlockErrorCode.ALREADY_BLOCKED;
import static org.websoso.WSSServer.exception.block.BlockErrorCode.SELF_BLOCKED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Block;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.exception.block.exception.AlreadyBlockedException;
import org.websoso.WSSServer.exception.block.exception.SelfBlockedException;
import org.websoso.WSSServer.repository.BlockRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

    private final BlockRepository blockRepository;

    @Transactional
    public void block(User blocker, Long blockedId) {
        Long blockingId = blocker.getUserId();
        if (blockingId.equals(blockedId)) {
            throw new SelfBlockedException(SELF_BLOCKED, "cannot block yourself");
        }

        if (blockRepository.existsByBlockingIdAndBlockedId(blockingId, blockedId)) {
            throw new AlreadyBlockedException(ALREADY_BLOCKED, "account has already been blocked");
        }

        blockRepository.save(Block.create(blockingId, blockedId));
    }
}
