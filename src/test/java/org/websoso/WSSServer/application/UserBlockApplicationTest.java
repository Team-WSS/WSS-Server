package org.websoso.WSSServer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.user.exception.CustomBlockError.INVALID_AUTHORIZED_BLOCK;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.dto.block.BlockGetResponse;
import org.websoso.WSSServer.dto.block.BlocksGetResponse;
import org.websoso.WSSServer.user.domain.Block;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.CustomBlockException;
import org.websoso.WSSServer.user.service.BlockQueryService;
import org.websoso.WSSServer.user.service.BlockService;
import org.websoso.WSSServer.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserBlockApplicationTest {

    @InjectMocks
    private UserBlockApplication userBlockApplication;

    @Mock
    private UserService userService;

    @Mock
    private BlockQueryService blockQueryService;

    @Mock
    private BlockService blockService;

    @Mock
    private User user;

    @Mock
    private User blockedUser;

    @DisplayName("차단 목록 조회 결과를 최근 차단 순서 그대로 반환한다")
    @Test
    void getsBlockListInQueryOrder() {
        BlockGetResponse recentBlock = new BlockGetResponse(2L, 20L, "최근 차단", "recent.png");
        BlockGetResponse oldBlock = new BlockGetResponse(1L, 10L, "이전 차단", "old.png");
        given(user.getUserId()).willReturn(1L);
        given(blockQueryService.findBlockRows(1L)).willReturn(List.of(recentBlock, oldBlock));

        BlocksGetResponse response = userBlockApplication.getBlockList(user);

        assertThat(response.blocks()).containsExactly(recentBlock, oldBlock);
    }

    @DisplayName("v2 차단 요청은 검증 후 v2 저장 로직을 호출한다")
    @Test
    void blocksUserWithV2Service() {
        given(user.isSameUserId(2L)).willReturn(false);
        given(userService.getUserOrException(2L)).willReturn(blockedUser);
        given(blockedUser.isAdmin()).willReturn(false);

        userBlockApplication.blockV2(user, 2L);

        then(blockService).should().createBlockV2(user, blockedUser);
    }

    @DisplayName("차단한 사용자는 자신의 차단 관계를 삭제할 수 있다")
    @Test
    void deletesOwnedBlock() {
        Block block = Block.create(1L, 2L);
        given(user.getUserId()).willReturn(1L);
        given(blockService.getBlockOrException(10L)).willReturn(block);

        userBlockApplication.deleteBlock(user, 10L);

        then(blockService).should().unblock(block);
    }

    @DisplayName("차단하지 않은 사용자는 다른 사용자의 차단 관계를 삭제할 수 없다")
    @Test
    void rejectsDeletingAnotherUsersBlock() {
        Block block = Block.create(1L, 2L);
        given(user.getUserId()).willReturn(3L);
        given(blockService.getBlockOrException(10L)).willReturn(block);

        assertThatThrownBy(() -> userBlockApplication.deleteBlock(user, 10L))
                .isInstanceOf(CustomBlockException.class)
                .extracting(throwable -> ((CustomBlockException) throwable).getICustomError())
                .isEqualTo(INVALID_AUTHORIZED_BLOCK);

        then(blockService).should(never()).unblock(block);
    }
}
