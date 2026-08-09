package org.websoso.WSSServer.collection.domain;

import static org.websoso.WSSServer.collection.exception.CustomCollectionError.PRIVATE_COLLECTION_ACCESS;

import org.websoso.WSSServer.collection.exception.CustomCollectionException;

/**
 * 컬렉션 접근 정책을 판단하는 데 필요한 값만 담은 컬렉션 요약.
 * <p>
 * 비공개 판정은 컬렉션 자체의 규칙이므로 도메인이 소유하지만, 판정에 필요한 값은 식별자·소유자·공개 여부뿐이다.
 * 값만 담으므로 조회 트랜잭션이 끝난 뒤에도 지연 로딩이나 준영속 엔티티 문제 없이 그대로 쓸 수 있고,
 * 다음 트랜잭션에 바깥 영속성 컨텍스트가 관리하던 엔티티가 딸려 들어가지 않는다.
 */
public record CollectionAccess(Long collectionId, Long ownerId, boolean isPublic) {

    public boolean isOwnedBy(Long userId) {
        return userId != null && userId.equals(ownerId);
    }

    /**
     * 비공개 컬렉션은 소유자만 볼 수 있다. 상세 조회와 좋아요 등록·취소처럼 컬렉션을 볼 수 있어야
     * 할 수 있는 요청이 같은 판정을 쓴다.
     */
    public void validateVisibleTo(Long viewerId) {
        if (!isPublic && !isOwnedBy(viewerId)) {
            throw new CustomCollectionException(
                    PRIVATE_COLLECTION_ACCESS,
                    "only the owner of the collection can access a private collection"
            );
        }
    }
}
