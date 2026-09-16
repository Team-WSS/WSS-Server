package org.websoso.WSSServer.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.DUPLICATE_COLLECTION_NOVEL;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_AUTHORIZED_COLLECTION;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.websoso.WSSServer.collection.domain.Collection;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.exception.DuplicateCollectionNovelException;
import org.websoso.WSSServer.collection.repository.CollectionNovelConstraintViolationDetector;
import org.websoso.WSSServer.collection.repository.CollectionRepository;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    private static final long OWNER_ID = 1L;
    private static final long COLLECTION_ID = 100L;

    @InjectMocks
    private CollectionService collectionService;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CollectionNovelConstraintViolationDetector constraintViolationDetector;

    @DisplayName("컬렉션을 저장할 때 즉시 반영해 제약조건 위반을 확인한다")
    @Test
    void flushesOnCreate() {
        Collection collection = collection();
        given(collectionRepository.saveAndFlush(collection)).willReturn(collection);

        assertThat(collectionService.create(collection)).isSameAs(collection);
        then(collectionRepository).should().saveAndFlush(collection);
    }

    @DisplayName("컬렉션 작품 유니크 제약 위반은 컬렉션 도메인 예외로 변환한다")
    @Test
    void translatesDuplicateCollectionNovelOnCreate() {
        Collection collection = collection();
        DataIntegrityViolationException violation = new DataIntegrityViolationException("duplicate");
        willThrow(violation).given(collectionRepository).saveAndFlush(collection);
        given(constraintViolationDetector.isDuplicateCollectionNovel(violation)).willReturn(true);

        assertThatThrownBy(() -> collectionService.create(collection))
                .isInstanceOf(DuplicateCollectionNovelException.class)
                .extracting(exception -> ((DuplicateCollectionNovelException) exception).getICustomError())
                .isEqualTo(DUPLICATE_COLLECTION_NOVEL);
    }

    @DisplayName("지정한 제약조건 위반이 아니면 데이터 무결성 오류를 숨기지 않는다")
    @Test
    void rethrowsUnrelatedDataIntegrityViolationOnCreate() {
        Collection collection = collection();
        DataIntegrityViolationException violation = new DataIntegrityViolationException("unrelated");
        willThrow(violation).given(collectionRepository).saveAndFlush(collection);
        given(constraintViolationDetector.isDuplicateCollectionNovel(violation)).willReturn(false);

        assertThatThrownBy(() -> collectionService.create(collection)).isSameAs(violation);
    }

    @DisplayName("수정 결과를 즉시 반영하고 컬렉션 작품 유니크 제약 위반을 도메인 예외로 변환한다")
    @Test
    void translatesDuplicateCollectionNovelOnFlush() {
        DataIntegrityViolationException violation = new DataIntegrityViolationException("duplicate");
        willThrow(violation).given(collectionRepository).flush();
        given(constraintViolationDetector.isDuplicateCollectionNovel(violation)).willReturn(true);

        assertThatThrownBy(collectionService::flushChanges)
                .isInstanceOf(DuplicateCollectionNovelException.class);
    }

    @DisplayName("수정 시 지정한 제약조건 위반이 아니면 데이터 무결성 오류를 숨기지 않는다")
    @Test
    void rethrowsUnrelatedDataIntegrityViolationOnFlush() {
        DataIntegrityViolationException violation = new DataIntegrityViolationException("unrelated");
        willThrow(violation).given(collectionRepository).flush();
        given(constraintViolationDetector.isDuplicateCollectionNovel(violation)).willReturn(false);

        assertThatThrownBy(collectionService::flushChanges).isSameAs(violation);
    }

    @DisplayName("컬렉션 조회는 포함 작품을 함께 가져온다")
    @Test
    void loadsCollectionWithNovels() {
        Collection collection = collection();
        given(collectionRepository.findByIdWithNovels(COLLECTION_ID)).willReturn(Optional.of(collection));

        assertThat(collectionService.getCollectionOrException(COLLECTION_ID)).isSameAs(collection);
        then(collectionRepository).should().findByIdWithNovels(COLLECTION_ID);
        then(collectionRepository).should(never()).findById(COLLECTION_ID);
    }

    @DisplayName("존재하지 않는 컬렉션은 404 컬렉션 도메인 예외로 처리한다")
    @Test
    void rejectsMissingCollection() {
        given(collectionRepository.findByIdWithNovels(COLLECTION_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.getCollectionOrException(COLLECTION_ID))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(COLLECTION_NOT_FOUND);
    }

    @DisplayName("소유자의 컬렉션만 수정·삭제 대상으로 조회하며 잠금 조회를 사용한다")
    @Test
    void returnsOwnedCollectionUsingLockingQuery() {
        Collection collection = collection(OWNER_ID);
        given(collectionRepository.findByIdForUpdate(COLLECTION_ID)).willReturn(Optional.of(collection));
        given(collectionRepository.findByIdWithNovels(COLLECTION_ID)).willReturn(Optional.of(collection));

        assertThat(collectionService.getOwnedCollectionOrException(COLLECTION_ID, OWNER_ID)).isSameAs(collection);

        InOrder inOrder = inOrder(collectionRepository);
        inOrder.verify(collectionRepository).findByIdForUpdate(COLLECTION_ID);
        inOrder.verify(collectionRepository).findByIdWithNovels(COLLECTION_ID);
    }

    @DisplayName("잠금 조회에서 컬렉션을 찾지 못하면 404로 처리하고 포함 작품을 조회하지 않는다")
    @Test
    void rejectsMissingCollectionOnLockingLookup() {
        given(collectionRepository.findByIdForUpdate(COLLECTION_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.getOwnedCollectionOrException(COLLECTION_ID, OWNER_ID))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(COLLECTION_NOT_FOUND);
        then(collectionRepository).should(never()).findByIdWithNovels(COLLECTION_ID);
    }

    @DisplayName("소유자가 아니면 403 컬렉션 도메인 예외로 처리하고 포함 작품을 조회하지 않는다")
    @Test
    void rejectsNonOwnedCollection() {
        Collection collection = collection(OWNER_ID);
        given(collectionRepository.findByIdForUpdate(COLLECTION_ID)).willReturn(Optional.of(collection));

        assertThatThrownBy(() -> collectionService.getOwnedCollectionOrException(COLLECTION_ID, 999L))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_AUTHORIZED_COLLECTION);
        then(collectionRepository).should(never()).findByIdWithNovels(COLLECTION_ID);
    }

    @DisplayName("조회 전용 경로는 잠금 조회를 사용하지 않는다")
    @Test
    void readOnlyLookupDoesNotLock() {
        Collection collection = collection();
        given(collectionRepository.findByIdWithNovels(COLLECTION_ID)).willReturn(Optional.of(collection));

        collectionService.getCollectionOrException(COLLECTION_ID);

        then(collectionRepository).should(never()).findByIdForUpdate(COLLECTION_ID);
    }

    @DisplayName("회원 탈퇴 시 소유 컬렉션의 소유자를 알 수 없는 사용자로 넘긴다")
    @Test
    void movesOwnedCollectionsToUnknownUserOnWithdrawal() {
        collectionService.updateOwnerToUnknown(OWNER_ID);

        then(collectionRepository).should().updateOwnerToUnknown(OWNER_ID);
    }

    @DisplayName("탈퇴 정리는 컬렉션과 컬렉션 작품을 삭제하지 않고 보존한다")
    @Test
    void withdrawalCleanupPreservesCollections() {
        collectionService.updateOwnerToUnknown(OWNER_ID);

        then(collectionRepository).should(never()).delete(any());
        then(collectionRepository).should(never()).deleteAll(any());
        then(collectionRepository).should(never()).deleteAllByIdInBatch(any());
        then(collectionRepository).should(never()).deleteAllInBatch(any());
        then(collectionRepository).should(never()).deleteAllInBatch();
    }

    @DisplayName("컬렉션 삭제는 저장소 삭제에 위임한다")
    @Test
    void deletesCollection() {
        Collection collection = collection();

        collectionService.delete(collection);

        then(collectionRepository).should().delete(collection);
    }

    private Collection collection() {
        return collection(mock(User.class));
    }

    private Collection collection(long ownerId) {
        User user = mock(User.class);
        given(user.getUserId()).willReturn(ownerId);

        return collection(user);
    }

    private Collection collection(User user) {
        Novel novel = mock(Novel.class);
        given(novel.getNovelId()).willReturn(1L);

        return Collection.create(user, "이름", "설명", true, List.of(novel), 1L);
    }
}
