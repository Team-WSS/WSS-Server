package org.websoso.WSSServer.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.dto.block.BlockGetResponse;
import org.websoso.WSSServer.user.repository.BlockQueryRepository;
import org.websoso.WSSServer.user.repository.projection.BlockInfoRow;

@ExtendWith(MockitoExtension.class)
class BlockQueryServiceTest {

    @InjectMocks
    private BlockQueryService blockQueryService;

    @Mock
    private BlockQueryRepository blockQueryRepository;

    @DisplayName("차단 조회 Row를 최근 차단 순서 그대로 응답으로 변환한다")
    @Test
    void convertsBlockRowsInQueryOrder() {
        BlockInfoRow recentBlock = new BlockInfoRow(2L, 20L, "최근 차단", "recent.png");
        BlockInfoRow oldBlock = new BlockInfoRow(1L, 10L, "이전 차단", "old.png");
        given(blockQueryRepository.findBlockInfoRows(1L)).willReturn(List.of(recentBlock, oldBlock));

        List<BlockGetResponse> result = blockQueryService.findBlockRows(1L);

        assertThat(result).containsExactly(
                new BlockGetResponse(2L, 20L, "최근 차단", "recent.png"),
                new BlockGetResponse(1L, 10L, "이전 차단", "old.png")
        );
    }
}
