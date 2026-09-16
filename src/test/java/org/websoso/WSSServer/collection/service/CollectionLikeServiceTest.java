package org.websoso.WSSServer.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.repository.CollectionLikeConstraintViolationDetector;
import org.websoso.WSSServer.collection.repository.CollectionLikeRepository;

@ExtendWith(MockitoExtension.class)
class CollectionLikeServiceTest {

    private static final long USER_ID = 1L;
    private static final long COLLECTION_ID = 100L;

    @InjectMocks
    private CollectionLikeService service;

    @Mock
    private CollectionLikeRepository collectionLikeRepository;

    @Mock
    private CollectionLikeConstraintViolationDetector constraintViolationDetector;

    // 등록

    @DisplayName("좋아요를 등록하면 사용자와 컬렉션으로 upsert를 실행한다")
    @Test
    void createsLike() {
        service.create(USER_ID, COLLECTION_ID);

        then(collectionLikeRepository).should().upsertLike(USER_ID, COLLECTION_ID);
    }

    /**
     * 중복은 upsert가 DB에서 흡수하므로 예외가 올라오지 않는다. 존재 여부를 미리 조회하면 동시 요청이
     * 둘 다 통과한 뒤 유니크 제약조건에서 갈리므로, 왕복만 늘고 중복을 막지도 못한다.
     */
    @DisplayName("등록 전에 이미 좋아요했는지 조회하지 않는다")
    @Test
    void doesNotCheckExistenceBeforeInsert() {
        service.create(USER_ID, COLLECTION_ID);

        then(collectionLikeRepository).should(never())
                .existsByUserIdAndCollectionCollectionId(anyLong(), anyLong());
    }

    @DisplayName("같은 요청을 반복해도 upsert 한 문장으로 끝난다")
    @Test
    void repeatedCreateStaysIdempotent() {
        assertThatCode(() -> {
            service.create(USER_ID, COLLECTION_ID);
            service.create(USER_ID, COLLECTION_ID);
        }).doesNotThrowAnyException();

        then(constraintViolationDetector).shouldHaveNoInteractions();
    }

    /**
     * MySQL의 갱신 행 수는 커넥터 설정(`CLIENT_FOUND_ROWS`)에 따라 중복에서 달라진다.
     * 등록 API는 "이번 호출이 무엇을 바꿨는지"가 아니라 최종 상태(204)만 보장하므로 결과를 내보내지 않는다.
     */
    @DisplayName("등록은 이번 호출이 실제로 행을 넣었는지 알리지 않는다")
    @Test
    void createReturnsNothing() {
        assertThat(findMethod(CollectionLikeService.class, "create").getReturnType()).isEqualTo(void.class);
    }

    // 트랜잭션 경계와 의존성

    /**
     * 잡아서 삼킬 예외가 없으므로 트랜잭션 경계를 코드로 다룰 이유가 없다.
     * 무결성 오류는 예외로 던져 이 트랜잭션을 롤백시킨다.
     */
    @DisplayName("등록의 트랜잭션 경계는 애너테이션 하나로 관리한다")
    @Test
    void createOwnsAnnotatedTransaction() {
        Transactional transactional = findMethod(CollectionLikeService.class, "create")
                .getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isFalse();
    }

    @DisplayName("트랜잭션 경계를 코드로 다루는 장치를 두지 않는다")
    @Test
    void hasNoProgrammaticTransactionCollaborator() {
        assertThat(Arrays.stream(CollectionLikeService.class.getDeclaredFields()))
                .noneMatch(field -> field.getType().getName().startsWith("org.springframework.transaction"));
    }

    /**
     * 여러 Service를 조합하는 것은 Application의 책임이다. Service가 다른 Service를 부르면
     * 유스케이스 트랜잭션 경계가 어디인지 코드에서 읽히지 않는다.
     */
    @DisplayName("다른 Service에 의존하지 않는다")
    @Test
    void dependsOnNoOtherService() {
        assertThat(Arrays.stream(CollectionLikeService.class.getDeclaredFields()))
                .noneMatch(field -> field.getType().getSimpleName().endsWith("Service"));
    }

    // 등록 실패 처리

    /**
     * 등록과 컬렉션 삭제의 경쟁은 외래 키 잠금 순서에 맡긴다. 삭제가 먼저면 등록이 외래 키 위반으로
     * 끝나는데, 이는 서버 오류가 아니라 컬렉션을 찾을 수 없는 요청이다(정책 16.7절).
     */
    @DisplayName("등록하는 사이에 컬렉션이 삭제됐으면 404 컬렉션 도메인 예외로 바꾼다")
    @Test
    void translatesMissingCollectionViolationToNotFound() {
        DataIntegrityViolationException violation = givenUpsertFails();
        given(constraintViolationDetector.isMissingCollection(violation)).willReturn(true);

        assertThatThrownBy(() -> service.create(USER_ID, COLLECTION_ID))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .satisfies(error -> {
                    assertThat(error).isEqualTo(COLLECTION_NOT_FOUND);
                    assertThat(COLLECTION_NOT_FOUND.getStatusCode().value()).isEqualTo(404);
                });
    }

    @DisplayName("판별하지 못한 무결성 오류는 숨기지 않는다")
    @Test
    void rethrowsUnrelatedIntegrityViolation() {
        DataIntegrityViolationException violation = givenUpsertFails();
        given(constraintViolationDetector.isMissingCollection(violation)).willReturn(false);

        assertThatThrownBy(() -> service.create(USER_ID, COLLECTION_ID)).isSameAs(violation);
    }

    /**
     * 어느 경우든 예외를 던지고 끝낸다. 무결성 오류를 삼키고 정상 반환하면 롤백 표시가 남은 트랜잭션을
     * 커밋하려다 `UnexpectedRollbackException`으로 끝난다.
     */
    @DisplayName("무결성 오류를 삼키고 정상 반환하지 않는다")
    @Test
    void neverSwallowsIntegrityViolation() {
        givenUpsertFails();
        given(constraintViolationDetector.isMissingCollection(any())).willReturn(true, false);

        assertThatThrownBy(() -> service.create(USER_ID, COLLECTION_ID)).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> service.create(USER_ID, COLLECTION_ID)).isInstanceOf(RuntimeException.class);
    }

    // 취소·조회

    @DisplayName("좋아요를 취소하면 지웠다고 알린다")
    @Test
    void reportsDeletedLike() {
        given(collectionLikeRepository.deleteLike(USER_ID, COLLECTION_ID)).willReturn(1L);

        assertThat(service.delete(USER_ID, COLLECTION_ID)).isTrue();
    }

    @DisplayName("좋아요하지 않은 컬렉션의 취소는 지울 행이 없을 뿐 오류가 아니다")
    @Test
    void deletingMissingLikeIsNotAnError() {
        given(collectionLikeRepository.deleteLike(USER_ID, COLLECTION_ID)).willReturn(0L);

        assertThat(service.delete(USER_ID, COLLECTION_ID)).isFalse();
    }

    @DisplayName("컬렉션의 좋아요를 한 번에 모두 지운다")
    @Test
    void deletesAllLikesOfCollection() {
        given(collectionLikeRepository.deleteAllByCollectionId(COLLECTION_ID)).willReturn(3L);

        assertThat(service.deleteAllByCollectionId(COLLECTION_ID)).isEqualTo(3L);
    }

    @DisplayName("컬렉션이 받은 좋아요 수를 센다")
    @Test
    void countsLikesOfCollection() {
        given(collectionLikeRepository.countByCollectionCollectionId(COLLECTION_ID)).willReturn(7L);

        assertThat(service.countByCollectionId(COLLECTION_ID)).isEqualTo(7L);
    }

    @DisplayName("조회자가 좋아요했는지 확인한다")
    @Test
    void checksWhetherViewerLiked() {
        given(collectionLikeRepository.existsByUserIdAndCollectionCollectionId(USER_ID, COLLECTION_ID))
                .willReturn(true);

        assertThat(service.isLikedBy(USER_ID, COLLECTION_ID)).isTrue();
    }

    @DisplayName("비로그인 조회자는 조회 없이 좋아요하지 않은 것으로 본다")
    @Test
    void anonymousViewerHasNotLiked() {
        assertThat(service.isLikedBy(null, COLLECTION_ID)).isFalse();

        then(collectionLikeRepository).should(never())
                .existsByUserIdAndCollectionCollectionId(any(), anyLong());
    }

    private DataIntegrityViolationException givenUpsertFails() {
        DataIntegrityViolationException violation = new DataIntegrityViolationException("violation");
        willThrow(violation).given(collectionLikeRepository).upsertLike(USER_ID, COLLECTION_ID);

        return violation;
    }

    private Method findMethod(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
