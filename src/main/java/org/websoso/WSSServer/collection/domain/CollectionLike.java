package org.websoso.WSSServer.collection.domain;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
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
import org.websoso.common.entity.BaseEntity;

/**
 * 사용자가 컬렉션에 누른 좋아요.
 * <p>
 * 컬렉션과 별도의 애그리거트다. 컬렉션 엔티티에 매핑하지 않으므로 컬렉션을 지울 때 좋아요는
 * {@code cascade}로 따라 지워지지 않고, 컬렉션 삭제 유스케이스가 명시적으로 먼저 정리한다.
 * <p>
 * {@code createdDate}는 "사용자가 좋아요를 누른 시점"을 뜻하며 좋아요한 컬렉션 목록의 정렬과
 * 커서 기준값이다. 같은 사용자와 컬렉션 조합의 좋아요는 하나만 저장하므로 좋아요를 취소했다가
 * 다시 누르면 새 행이 되고 목록에서도 다시 누른 시점으로 올라온다.
 * <p>
 * 좋아요를 누른 사용자는 {@code userId} 값만 저장하고 {@code User}를 매핑하지 않는다.
 * 피드 좋아요({@code like.user_id})와 같은 방식이며, 회원 탈퇴가 좋아요를 지우지 않아도 되도록
 * 사용자 행에 외래 키로 묶지 않는다.
 * <p>
 * 이 엔티티를 새로 만드는 생성자나 팩터리는 없다. 좋아요 등록은 중복을 DB가 흡수하도록 upsert 한 문장으로
 * 실행하므로(정책 16.2절) 영속성 컨텍스트를 거치지 않는다. 이 매핑은 조회·삭제 쿼리와 스키마 정의를
 * 담당한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "collection_like",
        uniqueConstraints = @UniqueConstraint(
                name = CollectionLike.UNIQUE_CONSTRAINT_NAME,
                columnNames = {"user_id", "collection_id"}
        ),
        indexes = @Index(
                name = CollectionLike.USER_LIKED_ORDER_INDEX_NAME,
                columnList = "user_id, created_date, collection_like_id"
        )
)
public class CollectionLike extends BaseEntity {

    public static final String UNIQUE_CONSTRAINT_NAME = "uk_collection_like_user_collection";
    public static final String USER_LIKED_ORDER_INDEX_NAME = "idx_collection_like_user_created";

    /**
     * 컬렉션을 참조하는 외래 키 이름. Hibernate가 붙이는 자동 생성 이름은 매핑이 바뀌면 같이 바뀌므로,
     * 실제 DB의 제약조건과 이 매핑이 같은 것을 가리킨다는 사실이 이름으로 드러나게 한다(정책 14.3절).
     */
    public static final String COLLECTION_FOREIGN_KEY_NAME = "fk_collection_like_collection";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long collectionLikeId;

    @Column(name = "user_id", nullable = false)
    @Comment("좋아요를 누른 사용자 PK")
    private Long userId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(
            name = "collection_id",
            nullable = false,
            foreignKey = @ForeignKey(name = CollectionLike.COLLECTION_FOREIGN_KEY_NAME)
    )
    @Comment("좋아요를 받은 컬렉션 PK")
    private Collection collection;

    public Long getCollectionId() {
        return collection.getCollectionId();
    }
}
