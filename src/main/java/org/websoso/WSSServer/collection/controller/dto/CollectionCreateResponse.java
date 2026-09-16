package org.websoso.WSSServer.collection.controller.dto;

import org.websoso.WSSServer.collection.domain.Collection;

/**
 * 같은 사용자가 같은 이름의 컬렉션을 여러 개 만들 수 있으므로 클라이언트는 이름으로 방금 만든
 * 컬렉션을 식별할 수 없다. 생성 직후 상세 조회와 공유를 위해 생성된 식별자를 반환한다.
 */
public record CollectionCreateResponse(
        Long collectionId
) {
    public static CollectionCreateResponse of(Collection collection) {
        return new CollectionCreateResponse(collection.getCollectionId());
    }
}
