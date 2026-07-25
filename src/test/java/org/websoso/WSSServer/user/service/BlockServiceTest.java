package org.websoso.WSSServer.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.websoso.WSSServer.user.exception.CustomBlockError.ALREADY_BLOCKED;
import static org.websoso.WSSServer.user.exception.CustomBlockError.BLOCKED_USER_ACCESS;
import static org.websoso.WSSServer.user.exception.CustomBlockError.BLOCK_NOT_FOUND;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.websoso.WSSServer.user.domain.Block;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.CustomBlockException;
import org.websoso.WSSServer.user.exception.DuplicateBlockException;
import org.websoso.WSSServer.user.repository.BlockConstraintViolationDetector;
import org.websoso.WSSServer.user.repository.BlockRepository;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

    @InjectMocks
    private BlockService blockService;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private BlockConstraintViolationDetector constraintViolationDetector;

    @Mock
    private User blocker;

    @Mock
    private User blocked;

    @DisplayName("기존 차단 로직은 신규 차단 관계를 저장한다")
    @Test
    void createsBlock() {
        given(blocker.getUserId()).willReturn(1L);
        given(blocked.getUserId()).willReturn(2L);

        blockService.createBlock(blocker, blocked);

        then(blockRepository).should().save(any(Block.class));
    }

    @DisplayName("기존 차단 로직은 저장 중 무결성 예외를 무시한다")
    @Test
    void ignoresIntegrityViolationInLegacyBlock() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("integrity violation");
        willThrow(exception).given(blockRepository).save(any(Block.class));

        assertThatCode(() -> blockService.createBlock(blocker, blocked))
                .doesNotThrowAnyException();

        then(constraintViolationDetector).shouldHaveNoInteractions();
    }

    @DisplayName("v2 차단 로직은 신규 차단 관계를 저장하고 즉시 flush한다")
    @Test
    void createsBlockV2() {
        given(blocker.getUserId()).willReturn(1L);
        given(blocked.getUserId()).willReturn(2L);

        blockService.createBlockV2(blocker, blocked);

        then(blockRepository).should().saveAndFlush(any(Block.class));
    }

    @DisplayName("중복 차단 제약조건 위반을 내부 중복 차단 예외로 변환한다")
    @Test
    void translatesDuplicateBlock() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate");
        willThrow(exception).given(blockRepository).saveAndFlush(any(Block.class));
        given(constraintViolationDetector.isDuplicateBlock(exception)).willReturn(true);

        assertThatThrownBy(() -> blockService.createBlockV2(blocker, blocked))
                .isInstanceOf(DuplicateBlockException.class)
                .extracting(throwable -> ((DuplicateBlockException) throwable).getICustomError())
                .isEqualTo(ALREADY_BLOCKED);
    }

    @DisplayName("중복 차단이 아닌 무결성 예외는 원본 예외를 유지한다")
    @Test
    void rethrowsUnrelatedConstraintViolation() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("unrelated");
        willThrow(exception).given(blockRepository).saveAndFlush(any(Block.class));
        given(constraintViolationDetector.isDuplicateBlock(exception)).willReturn(false);

        assertThatThrownBy(() -> blockService.createBlockV2(blocker, blocked))
                .isSameAs(exception);
    }

    @DisplayName("차단 ID에 해당하는 차단 관계를 조회한다")
    @Test
    void getsBlockById() {
        Block block = Block.create(1L, 2L);
        given(blockRepository.findById(10L)).willReturn(Optional.of(block));

        Block result = blockService.getBlockOrException(10L);

        assertThat(result).isSameAs(block);
    }

    @DisplayName("차단 ID에 해당하는 차단 관계가 없으면 예외가 발생한다")
    @Test
    void rejectsMissingBlock() {
        given(blockRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> blockService.getBlockOrException(10L))
                .isInstanceOf(CustomBlockException.class)
                .extracting(throwable -> ((CustomBlockException) throwable).getICustomError())
                .isEqualTo(BLOCK_NOT_FOUND);
    }

    @DisplayName("두 사용자 중 한쪽이라도 차단했으면 차단 관계로 판단한다")
    @Test
    void findsBlockRelationInEitherDirection() {
        given(blockRepository.existsBlockRelation(1L, 2L)).willReturn(true);

        boolean result = blockService.hasBlockRelation(1L, 2L);

        assertThat(result).isTrue();
    }

    @DisplayName("차단 관계인 사용자에게 접근하면 Block 도메인 예외가 발생한다")
    @Test
    void rejectsBlockedUserAccess() {
        given(blockRepository.existsBlockRelation(1L, 2L)).willReturn(true);

        assertThatThrownBy(() -> blockService.validateNotBlocked(1L, 2L))
                .isInstanceOf(CustomBlockException.class)
                .extracting(throwable -> ((CustomBlockException) throwable).getICustomError())
                .isEqualTo(BLOCKED_USER_ACCESS);
    }

    @DisplayName("본인 또는 비로그인 사용자는 차단 관계를 조회하지 않는다")
    @Test
    void skipsBlockRelationLookupWithoutTargetPair() {
        assertThat(blockService.hasBlockRelation(1L, 1L)).isFalse();
        assertThat(blockService.hasBlockRelation(null, 1L)).isFalse();

        then(blockRepository).shouldHaveNoInteractions();
    }

    @DisplayName("사용자와 양방향 차단 관계인 사용자 ID를 조회한다")
    @Test
    void findsRelatedUserIds() {
        given(blockRepository.findBlockRelationUserIds(1L)).willReturn(List.of(2L, 3L));

        List<Long> result = blockService.findBlockRelationUserIds(1L);

        assertThat(result).containsExactly(2L, 3L);
    }
}
