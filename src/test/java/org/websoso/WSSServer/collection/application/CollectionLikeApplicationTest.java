package org.websoso.WSSServer.collection.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.PRIVATE_COLLECTION_ACCESS;
import static org.websoso.WSSServer.user.exception.CustomBlockError.BLOCKED_USER_ACCESS;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.collection.domain.CollectionAccess;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.repository.CollectionLikeConstraintViolationDetector;
import org.websoso.WSSServer.collection.service.CollectionLikeService;
import org.websoso.WSSServer.collection.service.CollectionService;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.CustomBlockException;
import org.websoso.WSSServer.user.service.BlockService;

@ExtendWith(MockitoExtension.class)
class CollectionLikeApplicationTest {

    private static final long OWNER_ID = 1L;
    private static final long VISITOR_ID = 2L;
    private static final long COLLECTION_ID = 100L;

    @InjectMocks
    private CollectionLikeApplication application;

    @Mock
    private CollectionService collectionService;

    @Mock
    private CollectionLikeService collectionLikeService;

    @Mock
    private BlockService blockService;

    // 등록

    @DisplayName("공개 컬렉션에 좋아요를 등록한다")
    @Test
    void likesPublicCollection() {
        givenCollection(true);

        application.create(user(VISITOR_ID), COLLECTION_ID);

        then(collectionLikeService).should().create(VISITOR_ID, COLLECTION_ID);
    }

    @DisplayName("자신이 만든 컬렉션에도 좋아요를 등록할 수 있다")
    @Test
    void likesOwnCollection() {
        givenCollection(true);

        application.create(user(OWNER_ID), COLLECTION_ID);

        then(collectionLikeService).should().create(OWNER_ID, COLLECTION_ID);
    }

    @DisplayName("소유자는 자신의 비공개 컬렉션에도 좋아요를 등록할 수 있다")
    @Test
    void likesOwnPrivateCollection() {
        givenCollection(false);

        assertThatCode(() -> application.create(user(OWNER_ID), COLLECTION_ID)).doesNotThrowAnyException();
    }

    @DisplayName("다른 사용자의 비공개 컬렉션에는 좋아요를 등록할 수 없다")
    @Test
    void rejectsLikeOnOthersPrivateCollection() {
        givenCollection(false);

        assertThatThrownBy(() -> application.create(user(VISITOR_ID), COLLECTION_ID))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(PRIVATE_COLLECTION_ACCESS);
        then(collectionLikeService).should(never()).create(anyLong(), anyLong());
    }

    @DisplayName("존재하지 않는 컬렉션에는 좋아요를 등록할 수 없다")
    @Test
    void rejectsLikeOnUnknownCollection() {
        willThrow(new CustomCollectionException(COLLECTION_NOT_FOUND, "not found"))
                .given(collectionService).getCollectionAccessOrException(COLLECTION_ID);

        assertThatThrownBy(() -> application.create(user(VISITOR_ID), COLLECTION_ID))
                .isInstanceOf(CustomCollectionException.class);
        then(collectionLikeService).should(never()).create(anyLong(), anyLong());
    }

    @DisplayName("소유자와 차단 관계면 좋아요를 등록할 수 없다")
    @Test
    void rejectsLikeWhenBlocked() {
        givenCollection(true);
        givenBlocked();

        assertThatThrownBy(() -> application.create(user(VISITOR_ID), COLLECTION_ID))
                .isInstanceOf(CustomBlockException.class)
                .extracting(exception -> ((CustomBlockException) exception).getICustomError())
                .isEqualTo(BLOCKED_USER_ACCESS);
        then(collectionLikeService).should(never()).create(anyLong(), anyLong());
    }

    /**
     * 차단 관계라면 공개 컬렉션이어도 접근할 수 없으므로, 차단 검증이 공개 여부 검증보다 먼저다.
     * 비공개 컬렉션에서 두 정책이 모두 걸리면 차단 오류가 나가야 한다.
     */
    @DisplayName("차단 검증을 공개 여부 검증보다 먼저 한다")
    @Test
    void validatesBlockBeforeVisibility() {
        givenCollection(false);
        givenBlocked();

        assertThatThrownBy(() -> application.create(user(VISITOR_ID), COLLECTION_ID))
                .isInstanceOf(CustomBlockException.class);
    }

    @DisplayName("이미 좋아요한 컬렉션에 다시 등록해도 좋아요는 하나만 남는다")
    @Test
    void likeIsIdempotent() {
        givenCollection(true);

        application.create(user(VISITOR_ID), COLLECTION_ID);
        assertThatCode(() -> application.create(user(VISITOR_ID), COLLECTION_ID)).doesNotThrowAnyException();

        then(collectionLikeService).should(times(2)).create(VISITOR_ID, COLLECTION_ID);
    }

    // 트랜잭션 경계

    /**
     * 등록은 접근 검증(읽기) 뒤에 저장 한 건으로 끝나 함께 되돌릴 후속 작업이 없다. 바깥 트랜잭션을 열면
     * 저장 계층의 트랜잭션 블록이 그 트랜잭션에 참여하게 되어, 저장 실패가 유스케이스 트랜잭션까지
     * 롤백 전용으로 오염시키고 위반을 멱등하게 넘길 수 없다.
     */
    @DisplayName("좋아요 등록 유스케이스는 바깥 트랜잭션을 열지 않는다")
    @Test
    void likeUseCaseOpensNoOuterTransaction() {
        assertThat(CollectionLikeApplication.class.getAnnotation(Transactional.class)).isNull();
        assertThat(findMethod(CollectionLikeApplication.class, "create").getAnnotation(Transactional.class))
                .isNull();
    }

    /**
     * 취소는 제약조건 위반을 유스케이스 밖에서 받아야 할 이유가 없고, 접근 검증과 삭제를 한 트랜잭션에
     * 담아도 커넥션을 하나만 쓴다.
     */
    @DisplayName("좋아요 취소 유스케이스는 접근 검증과 삭제를 하나의 트랜잭션으로 묶는다")
    @Test
    void unlikeUseCaseOwnsOneTransaction() {
        assertThat(findMethod(CollectionLikeApplication.class, "delete").getAnnotation(Transactional.class))
                .isNotNull();
    }

    @DisplayName("좋아요 등록은 컬렉션 존재 확인, 차단 검증, 저장 순서로 각자의 트랜잭션을 쓴다")
    @Test
    void likeCallsServicesInOrder() {
        givenCollection(true);

        application.create(user(VISITOR_ID), COLLECTION_ID);

        InOrder inOrder = inOrder(collectionService, blockService, collectionLikeService);
        inOrder.verify(collectionService).getCollectionAccessOrException(COLLECTION_ID);
        inOrder.verify(blockService).validateNotBlocked(VISITOR_ID, OWNER_ID);
        inOrder.verify(collectionLikeService).create(VISITOR_ID, COLLECTION_ID);
    }

    /**
     * 검증 트랜잭션이 넘기는 것은 값뿐이다. 엔티티를 넘기면 그 인스턴스를 관리하던 영속성 컨텍스트가
     * 이미 닫힌 상태로 등록 트랜잭션에 들어가 지연 로딩이 깨진다.
     */
    @DisplayName("접근 검증은 컬렉션 엔티티를 읽지 않고 등록에도 식별자만 넘긴다")
    @Test
    void passesOnlyValuesToTheInsertTransaction() {
        givenCollection(true);

        application.create(user(VISITOR_ID), COLLECTION_ID);

        assertThat(findMethod(CollectionService.class, "getCollectionAccessOrException").getReturnType())
                .isEqualTo(CollectionAccess.class);
        assertThat(findMethod(CollectionLikeService.class, "create").getParameterTypes())
                .containsExactly(Long.class, Long.class);
    }

    // 저장 실패 처리는 저장하는 계층의 몫이다

    /**
     * 제약조건 위반을 멱등한 성공으로 볼지 사라진 컬렉션으로 볼지는 영속화의 사정이다.
     * 유스케이스가 판별기를 알면 어떤 제약조건이 어떤 이름으로 걸려 있는지가 유스케이스까지 새어 나온다.
     */
    @DisplayName("유스케이스는 좋아요 제약조건 판별기에 의존하지 않는다")
    @Test
    void doesNotDependOnConstraintViolationDetector() {
        assertThat(Arrays.stream(CollectionLikeApplication.class.getDeclaredFields()))
                .noneMatch(field -> field.getType().equals(CollectionLikeConstraintViolationDetector.class));
    }

    /**
     * 등록 실패의 판별은 등록하는 계층이 끝낸다. 여기까지 올라온 무결성 예외는
     * 유스케이스가 해석할 것이 아니므로 삼키거나 다른 예외로 바꾸지 않는다.
     */
    @DisplayName("등록 계층이 처리하지 않은 무결성 예외를 유스케이스가 대신 해석하지 않는다")
    @Test
    void doesNotInterpretPersistenceFailure() {
        givenCollection(true);
        DataIntegrityViolationException violation = new DataIntegrityViolationException("violation");
        willThrow(violation).given(collectionLikeService).create(VISITOR_ID, COLLECTION_ID);

        assertThatThrownBy(() -> application.create(user(VISITOR_ID), COLLECTION_ID)).isSameAs(violation);
    }

    /**
     * 등록하는 사이에 컬렉션이 삭제되면 등록 계층이 그 무결성 오류를 컬렉션 도메인 예외로 바꿔 올린다.
     * 유스케이스는 그것을 그대로 통과시킨다.
     */
    @DisplayName("등록 계층이 올린 컬렉션 도메인 예외를 그대로 통과시킨다")
    @Test
    void propagatesMissingCollectionFromInsert() {
        givenCollection(true);
        willThrow(new CustomCollectionException(COLLECTION_NOT_FOUND, "deleted while saving"))
                .given(collectionLikeService).create(VISITOR_ID, COLLECTION_ID);

        assertThatThrownBy(() -> application.create(user(VISITOR_ID), COLLECTION_ID))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(COLLECTION_NOT_FOUND);
    }

    // 취소

    @DisplayName("좋아요를 취소한다")
    @Test
    void unlikesCollection() {
        givenCollection(true);

        application.delete(user(VISITOR_ID), COLLECTION_ID);

        then(collectionLikeService).should().delete(VISITOR_ID, COLLECTION_ID);
    }

    @DisplayName("좋아요하지 않은 컬렉션의 취소도 오류 없이 끝난다")
    @Test
    void unlikeIsIdempotent() {
        givenCollection(true);
        given(collectionLikeService.delete(VISITOR_ID, COLLECTION_ID)).willReturn(false);

        assertThatCode(() -> application.delete(user(VISITOR_ID), COLLECTION_ID)).doesNotThrowAnyException();
        assertThatCode(() -> application.delete(user(VISITOR_ID), COLLECTION_ID)).doesNotThrowAnyException();
    }

    @DisplayName("존재하지 않는 컬렉션의 좋아요는 취소할 수 없다")
    @Test
    void rejectsUnlikeOnUnknownCollection() {
        willThrow(new CustomCollectionException(COLLECTION_NOT_FOUND, "not found"))
                .given(collectionService).getCollectionAccessOrException(COLLECTION_ID);

        assertThatThrownBy(() -> application.delete(user(VISITOR_ID), COLLECTION_ID))
                .isInstanceOf(CustomCollectionException.class);
        then(collectionLikeService).should(never()).delete(anyLong(), anyLong());
    }

    @DisplayName("다른 사용자의 비공개 컬렉션은 좋아요를 취소할 수도 없다")
    @Test
    void rejectsUnlikeOnOthersPrivateCollection() {
        givenCollection(false);

        assertThatThrownBy(() -> application.delete(user(VISITOR_ID), COLLECTION_ID))
                .isInstanceOf(CustomCollectionException.class);
        then(collectionLikeService).should(never()).delete(anyLong(), anyLong());
    }

    @DisplayName("소유자와 차단 관계면 좋아요를 취소할 수 없다")
    @Test
    void rejectsUnlikeWhenBlocked() {
        givenCollection(true);
        givenBlocked();

        assertThatThrownBy(() -> application.delete(user(VISITOR_ID), COLLECTION_ID))
                .isInstanceOf(CustomBlockException.class);
        then(collectionLikeService).should(never()).delete(anyLong(), anyLong());
    }

    private void givenCollection(boolean isPublic) {
        given(collectionService.getCollectionAccessOrException(COLLECTION_ID))
                .willReturn(new CollectionAccess(COLLECTION_ID, OWNER_ID, isPublic));
    }

    private void givenBlocked() {
        willThrow(new CustomBlockException(BLOCKED_USER_ACCESS, "blocked"))
                .given(blockService).validateNotBlocked(anyLong(), anyLong());
    }

    private Method findMethod(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private User user(Long userId) {
        User user = mock(User.class);
        given(user.getUserId()).willReturn(userId);
        return user;
    }
}
