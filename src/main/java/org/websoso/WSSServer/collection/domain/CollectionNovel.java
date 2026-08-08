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
 * {@code createdDate}는 "작품이 컬렉션에 추가된 시점"을 뜻하며 #561의 최근 추가 작품 미리보기와
 * 추가 시점 기준 정렬의 기준값이다. 따라서 컬렉션 수정 시 계속 포함되는 작품의 행은 절대 삭제 후
 * 재삽입하지 않는다. {@link Collection#update} 의 delta 갱신을 참고한다.
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
                name = CollectionNovel.COLLECTION_ADDED_ORDER_INDEX_NAME,
                columnList = "collection_id, created_date, collection_novel_id"
        )
)
public class CollectionNovel extends BaseEntity {

    public static final String UNIQUE_CONSTRAINT_NAME = "uk_collection_novel_collection_novel";
    public static final String COLLECTION_ADDED_ORDER_INDEX_NAME = "idx_collection_novel_collection_created";

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

    private CollectionNovel(Collection collection, Novel novel) {
        this.collection = collection;
        this.novel = novel;
    }

    public static CollectionNovel create(Collection collection, Novel novel) {
        return new CollectionNovel(collection, novel);
    }

    public Long getNovelId() {
        return novel.getNovelId();
    }
}
