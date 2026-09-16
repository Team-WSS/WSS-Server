package org.websoso.WSSServer.collection.domain;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.common.entity.BaseEntity;

/**
 * 컬렉션에 포함된 작품.
 * <p>
 * {@code displayOrder}는 클라이언트가 요청 배열로 정한 표시 순서다. 작은 값이 앞(최신·우선)이고
 * 큰 값이 뒤(오래된 작품)이며, 컬렉션의 모든 조회가 이 값 하나로 순서를 정한다.
 * {@link Collection}이 생성과 수정에서 포함 작품 전체에 {@code 0}부터 연속된 값을 다시 매기므로
 * 한 컬렉션 안의 표시 순서는 항상 {@code 0..n-1}이고 비거나 중복되지 않는다. 컬렉션 목록의 미리보기가
 * 상위 N개를 {@code displayOrder < N}으로 고를 수 있는 근거가 이 불변식이다.
 * <p>
 * {@code createdDate}는 "작품이 컬렉션에 추가된 시점"을 뜻한다. 표시 순서와 별개의 값이므로 재정렬만으로는
 * 바뀌지 않으며, 컬렉션 수정 시 계속 포함되는 작품의 행은 절대 삭제 후 재삽입하지 않는다.
 * {@link Collection#update} 의 delta 갱신을 참고한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "collection_novel",
        uniqueConstraints = @UniqueConstraint(
                name = CollectionNovel.UNIQUE_CONSTRAINT_NAME,
                columnNames = {"collection_id", "novel_id"}
        ),
        indexes = @Index(
                name = CollectionNovel.COLLECTION_DISPLAY_ORDER_INDEX_NAME,
                columnList = "collection_id, display_order"
        )
)
public class CollectionNovel extends BaseEntity {

    public static final String UNIQUE_CONSTRAINT_NAME = "uk_collection_novel_collection_novel";
    public static final String COLLECTION_DISPLAY_ORDER_INDEX_NAME = "idx_collection_novel_collection_display_order";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long collectionNovelId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    @Comment("작품이 포함된 컬렉션 PK")
    private Collection collection;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    @Comment("컬렉션에 포함된 작품 PK")
    private Novel novel;

    @Column(name = "display_order", nullable = false)
    @Comment("클라이언트가 정한 표시 순서, 컬렉션 안에서 0부터 연속하며 작을수록 앞에 온다")
    private int displayOrder;

    private CollectionNovel(Collection collection, Novel novel, int displayOrder) {
        this.collection = collection;
        this.novel = novel;
        this.displayOrder = displayOrder;
    }

    public static CollectionNovel create(Collection collection, Novel novel, int displayOrder) {
        return new CollectionNovel(collection, novel, displayOrder);
    }

    /**
     * 표시 순서만 바꾼다. 컬렉션에 추가된 시점은 재정렬로 바뀌는 값이 아니므로 건드리지 않는다.
     */
    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Long getNovelId() {
        return novel.getNovelId();
    }
}
