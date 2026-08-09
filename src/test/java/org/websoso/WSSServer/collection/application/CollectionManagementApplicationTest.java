package org.websoso.WSSServer.collection.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.DUPLICATE_COLLECTION_NOVEL;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_AUTHORIZED_COLLECTION;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_NOVEL_COUNT;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.REPRESENTATIVE_NOVEL_NOT_INCLUDED;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.collection.controller.dto.CollectionCreateRequest;
import org.websoso.WSSServer.collection.controller.dto.CollectionCreateResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionUpdateRequest;
import org.websoso.WSSServer.collection.domain.Collection;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.service.CollectionLikeService;
import org.websoso.WSSServer.collection.service.CollectionService;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.user.domain.User;

@ExtendWith(MockitoExtension.class)
class CollectionManagementApplicationTest {

    private static final long OWNER_ID = 1L;
    private static final long COLLECTION_ID = 100L;

    @InjectMocks
    private CollectionManagementApplication application;

    @Mock
    private CollectionService collectionService;

    @Mock
    private CollectionLikeService collectionLikeService;

    @Mock
    private NovelServiceImpl novelService;

    private final Map<Long, Novel> novelCache = new HashMap<>();
    private final User owner = user(OWNER_ID);

    @DisplayName("컬렉션을 생성하고 생성된 컬렉션 ID를 반환한다")
    @Test
    void createsCollectionAndReturnsId() {
        givenExistingNovels(1L, 2L);
        givenCreateAssignsCollectionId();

        CollectionCreateResponse response = application.create(owner, createRequest(List.of(1L, 2L), 2L));

        assertThat(response.collectionId()).isEqualTo(COLLECTION_ID);

        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        then(collectionService).should().create(captor.capture());
        Collection saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(owner);
        assertThat(saved.toCollectionNovelIds()).containsExactly(1L, 2L);
        assertThat(saved.getRepresentativeNovelId()).isEqualTo(2L);
    }

    @DisplayName("작품 존재 여부는 작품 수와 무관하게 한 번의 조회로 확인한다")
    @Test
    void validatesNovelExistenceWithSingleQuery() {
        givenExistingNovels(novelIdArray(100));
        givenCreateAssignsCollectionId();

        application.create(owner, createRequest(novelIds(100), 1L));

        then(novelService).should().findAllByIds(novelIds(100));
        then(novelService).should(never()).getNovelOrException(anyLong());
        then(novelService).should(never()).validateNovelExistsIfPresent(anyLong());
    }

    @DisplayName("요청한 작품이 요청 배열 순서대로 컬렉션에 추가된다")
    @Test
    void addsNovelsInRequestedOrderRegardlessOfQueryOrder() {
        givenFoundNovels(List.of(30L, 10L, 20L), List.of(10L, 20L, 30L));
        givenCreateAssignsCollectionId();

        application.create(owner, createRequest(List.of(30L, 10L, 20L), 10L));

        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        then(collectionService).should().create(captor.capture());
        assertThat(captor.getValue().toCollectionNovelIds()).containsExactly(30L, 10L, 20L);
    }

    @DisplayName("존재하지 않는 작품이 있으면 작품 도메인 예외로 처리하고 저장하지 않는다")
    @Test
    void rejectsMissingNovelOnCreate() {
        givenFoundNovels(List.of(1L, 2L), List.of(1L));

        assertThatThrownBy(() -> application.create(owner, createRequest(List.of(1L, 2L), 1L)))
                .isInstanceOf(CustomNovelException.class)
                .extracting(exception -> ((CustomNovelException) exception).getICustomError())
                .isEqualTo(NOVEL_NOT_FOUND);
        then(collectionService).should(never()).create(any());
    }

    @DisplayName("작품 정책을 위반하면 작품을 조회하지 않고 컬렉션 도메인 예외로 처리한다")
    @Test
    void validatesNovelPolicyBeforeQueryingNovels() {
        assertThatThrownBy(() -> application.create(owner, createRequest(List.of(), 1L)))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_COLLECTION_NOVEL_COUNT);

        assertThatThrownBy(() -> application.create(owner, createRequest(List.of(1L, 1L), 1L)))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(DUPLICATE_COLLECTION_NOVEL);

        assertThatThrownBy(() -> application.create(owner, createRequest(List.of(1L, 2L), 3L)))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(REPRESENTATIVE_NOVEL_NOT_INCLUDED);

        then(novelService).should(never()).findAllByIds(any());
        then(collectionService).should(never()).create(any());
    }

    @DisplayName("컬렉션을 수정하면 유지된 작품은 그대로 두고 변경분만 반영한 뒤 즉시 반영한다")
    @Test
    void updatesCollectionWithNovelDelta() {
        Collection collection = existingCollection(List.of(1L, 2L, 3L), 1L);
        given(collectionService.getOwnedCollectionOrException(COLLECTION_ID, OWNER_ID)).willReturn(collection);
        givenExistingNovels(2L, 3L, 4L);

        application.update(owner, COLLECTION_ID, updateRequest(List.of(2L, 3L, 4L), 2L));

        assertThat(collection.toCollectionNovelIds()).containsExactlyInAnyOrder(2L, 3L, 4L);
        assertThat(collection.getRepresentativeNovelId()).isEqualTo(2L);
        then(collectionService).should().flushChanges();
    }

    @DisplayName("존재하지 않는 컬렉션을 수정하면 컬렉션 도메인 예외로 처리하고 작품을 조회하지 않는다")
    @Test
    void rejectsUpdateOfMissingCollection() {
        willThrow(new CustomCollectionException(COLLECTION_NOT_FOUND, "not found"))
                .given(collectionService).getOwnedCollectionOrException(COLLECTION_ID, OWNER_ID);

        assertThatThrownBy(() -> application.update(owner, COLLECTION_ID, updateRequest(List.of(1L), 1L)))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(COLLECTION_NOT_FOUND);
        then(novelService).should(never()).findAllByIds(any());
        then(collectionService).should(never()).flushChanges();
    }

    @DisplayName("소유자가 아니면 컬렉션을 수정할 수 없다")
    @Test
    void rejectsUpdateByNonOwner() {
        willThrow(new CustomCollectionException(INVALID_AUTHORIZED_COLLECTION, "not owner"))
                .given(collectionService).getOwnedCollectionOrException(COLLECTION_ID, OWNER_ID);

        assertThatThrownBy(() -> application.update(owner, COLLECTION_ID, updateRequest(List.of(1L), 1L)))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_AUTHORIZED_COLLECTION);
        then(collectionService).should(never()).flushChanges();
    }

    @DisplayName("수정 요청의 작품이 존재하지 않으면 즉시 반영하지 않는다")
    @Test
    void doesNotFlushWhenUpdatedNovelIsMissing() {
        Collection collection = existingCollection(List.of(1L), 1L);
        given(collectionService.getOwnedCollectionOrException(COLLECTION_ID, OWNER_ID)).willReturn(collection);
        givenFoundNovels(List.of(1L, 2L), List.of(1L));

        assertThatThrownBy(() -> application.update(owner, COLLECTION_ID, updateRequest(List.of(1L, 2L), 1L)))
                .isInstanceOf(CustomNovelException.class);
        assertThat(collection.toCollectionNovelIds()).containsExactly(1L);
        then(collectionService).should(never()).flushChanges();
    }

    @DisplayName("소유자는 컬렉션을 삭제할 수 있다")
    @Test
    void deletesOwnedCollection() {
        Collection collection = existingCollection(List.of(1L), 1L);
        given(collectionService.getOwnedCollectionOrException(COLLECTION_ID, OWNER_ID)).willReturn(collection);

        application.delete(owner, COLLECTION_ID);

        then(collectionService).should().delete(collection);
    }

    @DisplayName("소유자가 아니면 컬렉션을 삭제할 수 없다")
    @Test
    void rejectsDeleteByNonOwner() {
        willThrow(new CustomCollectionException(INVALID_AUTHORIZED_COLLECTION, "not owner"))
                .given(collectionService).getOwnedCollectionOrException(COLLECTION_ID, OWNER_ID);

        assertThatThrownBy(() -> application.delete(owner, COLLECTION_ID))
                .isInstanceOf(CustomCollectionException.class);
        then(collectionService).should(never()).delete(any());
    }

    /**
     * 컬렉션 좋아요는 컬렉션과 별도 애그리거트라 cascade로 따라 지워지지 않는다. 좋아요 행이 컬렉션을
     * 외래 키로 참조하므로 남아 있으면 컬렉션 삭제 자체가 실패한다. 따라서 순서까지 확인한다.
     */
    @DisplayName("컬렉션을 삭제하면 그 컬렉션의 좋아요를 컬렉션보다 먼저 지운다")
    @Test
    void deletesCollectionLikesBeforeCollection() {
        Collection collection = existingCollection(List.of(1L), 1L);
        given(collectionService.getOwnedCollectionOrException(COLLECTION_ID, OWNER_ID)).willReturn(collection);

        application.delete(owner, COLLECTION_ID);

        InOrder inOrder = inOrder(collectionLikeService, collectionService);
        inOrder.verify(collectionLikeService).deleteAllByCollectionId(COLLECTION_ID);
        inOrder.verify(collectionService).delete(collection);
    }

    @DisplayName("소유자가 아니면 좋아요도 지우지 않는다")
    @Test
    void keepsCollectionLikesWhenDeleteIsRejected() {
        willThrow(new CustomCollectionException(INVALID_AUTHORIZED_COLLECTION, "not owner"))
                .given(collectionService).getOwnedCollectionOrException(COLLECTION_ID, OWNER_ID);

        assertThatThrownBy(() -> application.delete(owner, COLLECTION_ID))
                .isInstanceOf(CustomCollectionException.class);
        then(collectionLikeService).should(never()).deleteAllByCollectionId(anyLong());
    }

    private Collection existingCollection(List<Long> novelIds, Long representativeNovelId) {
        Collection collection = Collection.create(owner, "이름", "설명", true, novels(novelIds), representativeNovelId);
        ReflectionTestUtils.setField(collection, "collectionId", COLLECTION_ID);
        return collection;
    }

    private CollectionCreateRequest createRequest(List<Long> novelIds, Long representativeNovelId) {
        return new CollectionCreateRequest("이름", "설명", true, novelIds, representativeNovelId);
    }

    private CollectionUpdateRequest updateRequest(List<Long> novelIds, Long representativeNovelId) {
        return new CollectionUpdateRequest("수정된 이름", "설명", true, novelIds, representativeNovelId);
    }

    private void givenCreateAssignsCollectionId() {
        given(collectionService.create(any(Collection.class))).willAnswer(invocation -> {
            Collection collection = invocation.getArgument(0);
            ReflectionTestUtils.setField(collection, "collectionId", COLLECTION_ID);
            return collection;
        });
    }

    private void givenExistingNovels(Long... novelIds) {
        givenExistingNovels(List.of(novelIds));
    }

    private void givenExistingNovels(List<Long> novelIds) {
        givenFoundNovels(novelIds, novelIds);
    }

    /**
     * 작품 mock을 미리 만들어 두고 나서 조회 결과를 스텁한다.
     * {@code given(...)} 호출 안에서 다른 mock을 스텁하면 Mockito가 스텁이 끝나지 않은 것으로 본다.
     */
    private void givenFoundNovels(List<Long> requestedNovelIds, List<Long> foundNovelIds) {
        List<Novel> found = novels(foundNovelIds);
        given(novelService.findAllByIds(requestedNovelIds)).willReturn(found);
    }

    private List<Novel> novels(List<Long> novelIds) {
        return novelIds.stream()
                .map(this::novel)
                .toList();
    }

    private Novel novel(Long novelId) {
        return novelCache.computeIfAbsent(novelId, id -> {
            Novel novel = mock(Novel.class);
            given(novel.getNovelId()).willReturn(id);
            return novel;
        });
    }

    private List<Long> novelIds(int count) {
        List<Long> novelIds = new ArrayList<>();
        for (long novelId = 1; novelId <= count; novelId++) {
            novelIds.add(novelId);
        }
        return novelIds;
    }

    private Long[] novelIdArray(int count) {
        return novelIds(count).toArray(new Long[0]);
    }

    private User user(long userId) {
        User user = mock(User.class);
        given(user.getUserId()).willReturn(userId);
        return user;
    }
}
