package org.websoso.WSSServer.collection.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.DUPLICATE_COLLECTION_NOVEL;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_AUTHORIZED_COLLECTION;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_NOVEL_COUNT;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.REPRESENTATIVE_NOVEL_NOT_INCLUDED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.common.exception.ICustomError;

class CollectionTest {

    private static final long OWNER_ID = 1L;

    private final Map<Long, Novel> novelCache = new HashMap<>();
    private final User owner = owner(OWNER_ID);

    @DisplayName("컬렉션을 생성하면 요청한 작품이 요청 순서대로 포함된다")
    @Test
    void createsCollectionWithNovelsInRequestedOrder() {
        Collection collection = create(List.of(30L, 10L, 20L), 20L);

        assertThat(collection.toCollectionNovelIds()).containsExactly(30L, 10L, 20L);
        assertThat(collection.getRepresentativeNovelId()).isEqualTo(20L);
        assertThat(collection.getUser()).isSameAs(owner);
    }

    @DisplayName("생성 시 공개 여부를 생략하면 공개 컬렉션으로 만든다")
    @Test
    void createsPublicCollectionWhenIsPublicIsOmitted() {
        Collection collection = Collection.create(owner, "이름", null, null, novels(List.of(1L)), 1L);

        assertThat(collection.isPublic()).isTrue();
    }

    @DisplayName("생성 시 공개 여부를 명시하면 그대로 사용한다")
    @Test
    void createsPrivateCollectionWhenIsPublicIsFalse() {
        Collection collection = Collection.create(owner, "이름", null, false, novels(List.of(1L)), 1L);

        assertThat(collection.isPublic()).isFalse();
    }

    @DisplayName("수정은 공개 여부를 양방향으로 바꾼다")
    @Test
    void updateAppliesPublicFlagBothWays() {
        Collection collection = Collection.create(owner, "이름", null, true, novels(List.of(1L)), 1L);

        collection.update("이름", null, false, novels(List.of(1L)), 1L);
        assertThat(collection.isPublic()).isFalse();

        collection.update("이름", null, true, novels(List.of(1L)), 1L);
        assertThat(collection.isPublic()).isTrue();
    }

    @DisplayName("설명이 없거나 공백뿐이면 null로 저장한다")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void normalizesBlankDescriptionToNull(String description) {
        Collection collection = Collection.create(owner, "이름", description, true, novels(List.of(1L)), 1L);

        assertThat(collection.getDescription()).isNull();
    }

    @DisplayName("컬렉션 이름의 앞뒤 공백을 제거하지 않는다")
    @Test
    void doesNotTrimName() {
        Collection collection = Collection.create(owner, "  나의 컬렉션  ", null, true, novels(List.of(1L)), 1L);

        assertThat(collection.getName()).isEqualTo("  나의 컬렉션  ");
    }

    @DisplayName("작품 수가 1개 이상 100개 이하이면 정책 검증을 통과한다")
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 99, 100})
    void acceptsNovelCountWithinBounds(int novelCount) {
        List<Long> novelIds = novelIds(novelCount);

        assertThatCode(() -> Collection.validateNovelPolicy(novelIds, novelIds.get(0)))
                .doesNotThrowAnyException();
    }

    @DisplayName("작품이 없거나 100개를 초과하면 컬렉션 도메인 예외로 처리한다")
    @ParameterizedTest
    @ValueSource(ints = {0, 101, 200})
    void rejectsNovelCountOutOfBounds(int novelCount) {
        List<Long> novelIds = novelIds(novelCount);

        assertThatThrownBy(() -> Collection.validateNovelPolicy(novelIds, 1L))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_COLLECTION_NOVEL_COUNT);
    }

    @DisplayName("작품 목록이 null이면 컬렉션 도메인 예외로 처리한다")
    @Test
    void rejectsNullNovelIds() {
        assertThatThrownBy(() -> Collection.validateNovelPolicy(null, 1L))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_COLLECTION_NOVEL_COUNT);
    }

    @DisplayName("같은 작품을 중복으로 포함하면 컬렉션 도메인 예외로 처리한다")
    @Test
    void rejectsDuplicateNovel() {
        assertThatThrownBy(() -> Collection.validateNovelPolicy(List.of(1L, 2L, 1L), 1L))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(DUPLICATE_COLLECTION_NOVEL);
    }

    @DisplayName("대표 작품이 포함 작품이 아니면 컬렉션 도메인 예외로 처리한다")
    @Test
    void rejectsRepresentativeNovelNotIncluded() {
        assertThatThrownBy(() -> Collection.validateNovelPolicy(List.of(1L, 2L), 3L))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(REPRESENTATIVE_NOVEL_NOT_INCLUDED);
    }

    @DisplayName("대표 작품이 null이면 컬렉션 도메인 예외로 처리한다")
    @Test
    void rejectsNullRepresentativeNovel() {
        assertThatThrownBy(() -> Collection.validateNovelPolicy(List.of(1L, 2L), null))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(REPRESENTATIVE_NOVEL_NOT_INCLUDED);
    }

    @DisplayName("생성 시에도 작품 정책 위반을 막는다")
    @Test
    void createValidatesNovelPolicy() {
        List<Novel> duplicated = novels(new ArrayList<>(List.of(1L, 1L)));

        assertThatThrownBy(() -> Collection.create(owner, "이름", null, true, duplicated, 1L))
                .isInstanceOf(CustomCollectionException.class);
    }

    @DisplayName("수정 시 계속 포함되는 작품의 컬렉션 작품 행을 그대로 유지해 추가 시점을 보존한다")
    @Test
    void preservesAddedTimeOfRetainedNovelsOnUpdate() {
        Collection collection = create(List.of(1L, 2L, 3L), 1L);

        LocalDateTime addedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        collection.getCollectionNovels().forEach(collectionNovel ->
                ReflectionTestUtils.setField(collectionNovel, "createdDate", addedAt));
        Map<Long, CollectionNovel> before = byNovelId(collection);

        collection.update("수정된 이름", null, true, novels(List.of(2L, 3L, 4L)), 2L);

        Map<Long, CollectionNovel> after = byNovelId(collection);
        assertThat(after.get(2L)).isSameAs(before.get(2L));
        assertThat(after.get(3L)).isSameAs(before.get(3L));
        assertThat(after.get(2L).getCreatedDate()).isEqualTo(addedAt);
        assertThat(after.get(3L).getCreatedDate()).isEqualTo(addedAt);
    }

    @DisplayName("포함 작품만 바뀌는 수정도 컬렉션 수정 시각을 갱신하고 유지 작품의 추가 시점은 보존한다")
    @Test
    void advancesModifiedDateWhenOnlyNovelsChange() {
        Collection collection = Collection.create(owner, "이름", "설명", true, novels(List.of(1L, 2L)), 1L);

        LocalDateTime addedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime staleModifiedDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        collection.getCollectionNovels().forEach(collectionNovel ->
                ReflectionTestUtils.setField(collectionNovel, "createdDate", addedAt));
        ReflectionTestUtils.setField(collection, "modifiedDate", staleModifiedDate);
        Map<Long, CollectionNovel> before = byNovelId(collection);

        // 이름, 설명, 공개 여부, 대표 작품은 그대로 두고 포함 작품만 바꾼다.
        collection.update("이름", "설명", true, novels(List.of(1L, 2L, 3L)), 1L);

        assertThat(collection.getModifiedDate()).isAfter(staleModifiedDate);
        assertThat(collection.toCollectionNovelIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat(byNovelId(collection).get(1L)).isSameAs(before.get(1L));
        assertThat(byNovelId(collection).get(2L)).isSameAs(before.get(2L));
        assertThat(byNovelId(collection).get(1L).getCreatedDate()).isEqualTo(addedAt);
        assertThat(byNovelId(collection).get(2L).getCreatedDate()).isEqualTo(addedAt);
    }

    @DisplayName("포함 작품과 스칼라 값이 모두 그대로여도 수정 시각을 갱신한다")
    @Test
    void advancesModifiedDateEvenWhenNothingEffectivelyChanges() {
        Collection collection = Collection.create(owner, "이름", "설명", true, novels(List.of(1L, 2L)), 1L);
        LocalDateTime staleModifiedDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        ReflectionTestUtils.setField(collection, "modifiedDate", staleModifiedDate);

        collection.update("이름", "설명", true, novels(List.of(1L, 2L)), 1L);

        assertThat(collection.getModifiedDate()).isAfter(staleModifiedDate);
    }

    @DisplayName("정책 위반으로 실패한 수정은 수정 시각을 갱신하지 않는다")
    @Test
    void doesNotAdvanceModifiedDateWhenUpdateFails() {
        Collection collection = Collection.create(owner, "이름", "설명", true, novels(List.of(1L, 2L)), 1L);
        LocalDateTime staleModifiedDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        ReflectionTestUtils.setField(collection, "modifiedDate", staleModifiedDate);
        List<Novel> requested = novels(List.of(3L, 4L));

        assertThatThrownBy(() -> collection.update("이름", "설명", true, requested, 1L))
                .isInstanceOf(CustomCollectionException.class);

        assertThat(collection.getModifiedDate()).isEqualTo(staleModifiedDate);
    }

    @DisplayName("수정 시 제거된 작품만 빠지고 새 작품만 추가된다")
    @Test
    void appliesNovelDeltaOnUpdate() {
        Collection collection = create(List.of(1L, 2L, 3L), 1L);
        Map<Long, CollectionNovel> before = byNovelId(collection);

        collection.update("수정된 이름", null, true, novels(List.of(2L, 3L, 4L)), 2L);

        assertThat(collection.toCollectionNovelIds()).containsExactlyInAnyOrder(2L, 3L, 4L);
        assertThat(collection.getCollectionNovels())
                .filteredOn(collectionNovel -> collectionNovel.getNovelId().equals(4L))
                .singleElement()
                .extracting(CollectionNovel::getCreatedDate)
                .isNull();
        assertThat(collection.getCollectionNovels()).doesNotContain(before.get(1L));
    }

    @DisplayName("포함 작품이 모두 그대로면 컬렉션 작품 행을 하나도 새로 만들지 않는다")
    @Test
    void keepsEveryCollectionNovelWhenNovelsAreUnchanged() {
        Collection collection = create(List.of(1L, 2L), 1L);
        List<CollectionNovel> before = List.copyOf(collection.getCollectionNovels());

        collection.update("수정된 이름", "설명", false, novels(List.of(1L, 2L)), 2L);

        assertThat(collection.getCollectionNovels()).containsExactlyInAnyOrderElementsOf(before);
    }

    @DisplayName("수정은 이름, 설명, 공개 여부, 대표 작품을 함께 반영한다")
    @Test
    void updatesCollectionAttributes() {
        Collection collection = create(List.of(1L, 2L), 1L);

        collection.update("수정된 이름", "  ", false, novels(List.of(1L, 2L)), 2L);

        assertThat(collection.getName()).isEqualTo("수정된 이름");
        assertThat(collection.getDescription()).isNull();
        assertThat(collection.isPublic()).isFalse();
        assertThat(collection.getRepresentativeNovelId()).isEqualTo(2L);
    }

    @DisplayName("수정 시에도 작품 정책 위반을 막고 기존 포함 작품을 바꾸지 않는다")
    @Test
    void updateValidatesNovelPolicyBeforeChangingNovels() {
        Collection collection = create(List.of(1L, 2L), 1L);
        List<Novel> requested = novels(List.of(3L, 4L));

        assertThatThrownBy(() -> collection.update("이름", null, true, requested, 1L))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(REPRESENTATIVE_NOVEL_NOT_INCLUDED);
        assertThat(collection.toCollectionNovelIds()).containsExactly(1L, 2L);
    }

    @DisplayName("소유자는 컬렉션을 수정하거나 삭제할 수 있다")
    @Test
    void allowsOwner() {
        Collection collection = create(List.of(1L), 1L);

        assertThat(collection.isOwnedBy(OWNER_ID)).isTrue();
        assertThatCode(() -> collection.validateOwner(OWNER_ID)).doesNotThrowAnyException();
    }

    @DisplayName("소유자가 아니면 403 컬렉션 도메인 예외로 처리한다")
    @Test
    void rejectsNonOwner() {
        Collection collection = create(List.of(1L), 1L);

        assertThat(collection.isOwnedBy(999L)).isFalse();
        assertThatThrownBy(() -> collection.validateOwner(999L))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .asInstanceOf(InstanceOfAssertFactories.type(ICustomError.class))
                .satisfies(error -> {
                    assertThat(error).isEqualTo(INVALID_AUTHORIZED_COLLECTION);
                    assertThat(error.getStatusCode().value()).isEqualTo(403);
                });
    }

    @DisplayName("비로그인 등으로 사용자 ID가 없으면 소유자로 보지 않는다")
    @Test
    void rejectsNullUserId() {
        Collection collection = create(List.of(1L), 1L);

        assertThat(collection.isOwnedBy(null)).isFalse();
    }

    /**
     * 접근 정책 판단에 필요한 값만 뽑아 두면 조회 트랜잭션이 닫힌 뒤에도 검증할 수 있고,
     * 다음 트랜잭션에 준영속 엔티티가 딸려 들어가지 않는다.
     */
    @DisplayName("접근 정책 판단에 필요한 식별자·소유자·공개 여부만 값으로 뽑아 낸다")
    @Test
    void extractsAccessValues() {
        Collection collection = Collection.create(owner, "이름", null, false, novels(List.of(1L)), 1L);
        ReflectionTestUtils.setField(collection, "collectionId", 100L);

        assertThat(collection.toAccess())
                .isEqualTo(new CollectionAccess(100L, OWNER_ID, false));
    }

    private Collection create(List<Long> novelIds, Long representativeNovelId) {
        return Collection.create(owner, "이름", "설명", true, novels(novelIds), representativeNovelId);
    }

    private Map<Long, CollectionNovel> byNovelId(Collection collection) {
        Map<Long, CollectionNovel> byNovelId = new HashMap<>();
        collection.getCollectionNovels()
                .forEach(collectionNovel -> byNovelId.put(collectionNovel.getNovelId(), collectionNovel));
        return byNovelId;
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

    private User owner(long userId) {
        User user = mock(User.class);
        given(user.getUserId()).willReturn(userId);
        return user;
    }
}
