package org.websoso.WSSServer.collection.domain;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.DUPLICATE_COLLECTION_NOVEL;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_AUTHORIZED_COLLECTION;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_NOVEL_COUNT;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.REPRESENTATIVE_NOVEL_NOT_INCLUDED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.common.entity.BaseEntity;

/**
 * 사용자가 작품을 주제별로 묶은 컬렉션.
 * <p>
 * 클래스명은 {@code java.util.Collection}과 단순 이름이 겹치지만, 이 패키지 안에서 컬렉션을 가리키는
 * 도메인 용어를 우선한다. 이 패키지의 코드는 {@code java.util.Collection}을 사용하지 않고 항상
 * {@code List}/{@code Set} 등 구체 타입을 쓴다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "collection",
        indexes = @Index(
                name = Collection.USER_CREATED_ORDER_INDEX_NAME,
                columnList = "user_id, created_date, collection_id"
        )
)
public class Collection extends BaseEntity {

    public static final String USER_CREATED_ORDER_INDEX_NAME = "idx_collection_user_created";

    public static final int MIN_NOVEL_COUNT = 1;
    public static final int MAX_NOVEL_COUNT = 100;
    public static final int MAX_NAME_LENGTH = 20;
    public static final int MAX_DESCRIPTION_LENGTH = 60;
    public static final boolean DEFAULT_IS_PUBLIC = true;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long collectionId;

    @Column(length = MAX_NAME_LENGTH, nullable = false)
    @Comment("컬렉션 이름")
    private String name;

    @Column(length = MAX_DESCRIPTION_LENGTH)
    @Comment("컬렉션 설명, 없으면 null")
    private String description;

    @Column(nullable = false)
    @Comment("공개 여부")
    private boolean isPublic;

    @Column(name = "representative_novel_id", nullable = false)
    @Comment("대표 작품 PK, 항상 컬렉션에 포함된 작품이어야 한다")
    private Long representativeNovelId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("컬렉션을 만든 사용자 PK")
    private User user;

    @OneToMany(mappedBy = "collection", cascade = ALL, orphanRemoval = true, fetch = LAZY)
    private List<CollectionNovel> collectionNovels = new ArrayList<>();

    /**
     * 공개 여부만 {@code Boolean}으로 받는다. 생성 요청에서는 값을 생략할 수 있어야 하고,
     * 생략({@code null})과 명시적인 {@code false}를 구분해야 기본값 공개를 적용할 수 있기 때문이다.
     * 기본값 적용은 DB {@code DEFAULT}가 아니라 이 생성자가 책임지므로, 어떤 경로로 생성해도
     * 공개 여부는 항상 결정된 값으로 저장된다.
     */
    private Collection(User user, String name, String description, Boolean isPublic, Long representativeNovelId) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic == null ? DEFAULT_IS_PUBLIC : isPublic;
        this.representativeNovelId = representativeNovelId;
    }

    /**
     * 컬렉션을 생성한다. 공개 여부를 생략하면 생성자가 기본값 공개를 적용한다.
     */
    public static Collection create(User user, String name, String description, Boolean isPublic,
                                    List<Novel> novels, Long representativeNovelId) {
        validateNovelPolicy(toNovelIds(novels), representativeNovelId);

        Collection collection = new Collection(
                user,
                name,
                normalizeDescription(description),
                isPublic,
                representativeNovelId
        );
        novels.forEach(novel -> collection.collectionNovels.add(CollectionNovel.create(collection, novel)));

        return collection;
    }

    /**
     * 컬렉션을 전체 교체 의미로 수정한다.
     * <p>
     * 포함 작품은 전체 삭제 후 재삽입하지 않고 delta로만 갱신한다. 계속 포함되는 작품의
     * {@link CollectionNovel} 행은 손대지 않으므로 컬렉션에 추가된 시점({@code createdDate})이 보존되고,
     * 같은 트랜잭션에서 같은 {@code (collection_id, novel_id)}를 지웠다가 다시 넣지 않으므로
     * 유니크 제약 위반도 발생하지 않는다.
     * <p>
     * 검증을 통과한 수정은 항상 컬렉션을 수정된 것으로 표시한다. 포함 작품만 바뀌면 컬렉션 행 자체는
     * 변경되지 않아 자동 갱신되는 {@code modifiedDate}가 그대로 남기 때문에, 자식만 바뀐 경우에도
     * 수정 시각이 바뀐다는 정책을 지키려면 명시적으로 표시해야 한다.
     */
    public void update(String name, String description, boolean isPublic,
                       List<Novel> novels, Long representativeNovelId) {
        validateNovelPolicy(toNovelIds(novels), representativeNovelId);

        this.name = name;
        this.description = normalizeDescription(description);
        this.isPublic = isPublic;
        this.representativeNovelId = representativeNovelId;
        applyNovelDelta(novels);
        touch();
    }

    private void applyNovelDelta(List<Novel> novels) {
        Set<Long> requestedNovelIds = new LinkedHashSet<>(toNovelIds(novels));

        collectionNovels.removeIf(collectionNovel -> !requestedNovelIds.contains(collectionNovel.getNovelId()));

        Set<Long> retainedNovelIds = new LinkedHashSet<>(toCollectionNovelIds());
        novels.stream()
                .filter(novel -> !retainedNovelIds.contains(novel.getNovelId()))
                .forEach(novel -> collectionNovels.add(CollectionNovel.create(this, novel)));
    }

    public void validateOwner(Long userId) {
        if (!isOwnedBy(userId)) {
            throw new CustomCollectionException(
                    INVALID_AUTHORIZED_COLLECTION,
                    "only the owner of the collection can modify or delete it"
            );
        }
    }

    public boolean isOwnedBy(Long userId) {
        return userId != null && userId.equals(user.getUserId());
    }

    public List<Long> toCollectionNovelIds() {
        return collectionNovels.stream()
                .map(CollectionNovel::getNovelId)
                .toList();
    }

    /**
     * 컬렉션 작품 정책을 검증한다. 작품 수, 중복 작품, 대표 작품 포함 여부는 모두 컬렉션 도메인 예외로 처리한다.
     */
    public static void validateNovelPolicy(List<Long> novelIds, Long representativeNovelId) {
        validateNovelCount(novelIds);
        validateNoDuplicateNovel(novelIds);
        validateRepresentativeNovelIncluded(novelIds, representativeNovelId);
    }

    private static void validateNovelCount(List<Long> novelIds) {
        if (novelIds == null || novelIds.size() < MIN_NOVEL_COUNT || novelIds.size() > MAX_NOVEL_COUNT) {
            throw new CustomCollectionException(
                    INVALID_COLLECTION_NOVEL_COUNT,
                    "collection must contain between " + MIN_NOVEL_COUNT + " and " + MAX_NOVEL_COUNT + " novels"
            );
        }
    }

    private static void validateNoDuplicateNovel(List<Long> novelIds) {
        if (new LinkedHashSet<>(novelIds).size() != novelIds.size()) {
            throw new CustomCollectionException(
                    DUPLICATE_COLLECTION_NOVEL,
                    "collection cannot contain the same novel more than once"
            );
        }
    }

    private static void validateRepresentativeNovelIncluded(List<Long> novelIds, Long representativeNovelId) {
        if (representativeNovelId == null || !novelIds.contains(representativeNovelId)) {
            throw new CustomCollectionException(
                    REPRESENTATIVE_NOVEL_NOT_INCLUDED,
                    "representative novel must be one of the novels included in the collection"
            );
        }
    }

    /**
     * 설명은 선택 항목이다. null, 빈 문자열, 공백만 있는 문자열을 모두 null로 정규화해서
     * 조회 응답이 "설명 없음"을 한 가지 값으로만 표현하도록 한다.
     */
    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description;
    }

    private static List<Long> toNovelIds(List<Novel> novels) {
        if (novels == null) {
            return null;
        }

        return novels.stream()
                .map(Novel::getNovelId)
                .toList();
    }
}
