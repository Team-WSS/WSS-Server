package org.websoso.WSSServer.collection.repository;

import java.util.Optional;
import org.websoso.WSSServer.collection.domain.Collection;

public interface CollectionCustomRepository {

    /**
     * 수정·삭제 대상 컬렉션을 쓰기 잠금으로 가져온다.
     * <p>
     * 컬렉션 루트 행만 잠근다. 같은 컬렉션을 동시에 수정하는 요청이 서로의 delta 계산을 덮어쓰지 못하게
     * 직렬화하는 데는 루트 행 잠금으로 충분하다. 포함 작품까지 조인해 잠그면 여러 컬렉션이 공유하는
     * {@code novel} 행을 잠그게 되어, 서로 무관한 컬렉션 수정끼리 대기하게 된다.
     * <p>
     * 조회 전용 경로는 이 메서드를 사용하지 않는다.
     */
    Optional<Collection> findByIdForUpdate(Long collectionId);

    /**
     * 포함 작품과 작품 정보를 함께 조회한다. 잠금을 걸지 않으므로 조회 전용 경로에서도 사용할 수 있다.
     * delta 계산과 cascade 삭제가 작품 수(최대 100)만큼 추가 조회를 일으키지 않도록 한다.
     */
    Optional<Collection> findByIdWithNovels(Long collectionId);

    /**
     * 회원 탈퇴 시 소유 컬렉션의 소유자를 알 수 없는 사용자로 넘긴다.
     * <p>
     * 컬렉션과 포함 작품을 지우지 않고 보존하는 것이 목적이므로, 엔티티를 불러오지 않고
     * 소유자 컬럼만 한 번에 갱신한다. 자식 행({@code collection_novel})은 손대지 않는다.
     */
    void updateOwnerToUnknown(Long userId);
}
