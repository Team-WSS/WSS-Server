package org.websoso.WSSServer.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.block.BlockGetResponse;
import org.websoso.WSSServer.user.repository.BlockQueryRepository;
import org.websoso.WSSServer.user.repository.projection.BlockInfoRow;

@Service
@RequiredArgsConstructor
public class BlockQueryService {

    private final BlockQueryRepository blockQueryRepository;

    @Transactional(readOnly = true)
    public List<BlockGetResponse> findBlockRows(Long blockingId) {
        return blockQueryRepository.findBlockInfoRows(blockingId).stream()
                .map(BlockInfoRow::toResponse)
                .toList();
    }
}
