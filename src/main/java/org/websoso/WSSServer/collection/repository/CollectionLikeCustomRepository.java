package org.websoso.WSSServer.collection.repository;

/**
 * 메서드 이름 파생 쿼리로 표현할 수 없는 컬렉션 좋아요 쿼리.
 * <p>
 * 삭제는 대상 행을 모두 엔티티로 읽은 뒤 한 행씩 지우지 않도록 QueryDSL 벌크 삭제로,
 * 등록은 중복을 예외가 아니라 DB가 흡수하도록 MySQL upsert로 명시한다.
 */
public interface CollectionLikeCustomRepository {

    /**
     * 좋아요를 등록한다. 이미 좋아요한 컬렉션이면 아무 행도 바꾸지 않는다.
     * <p>
     * 유니크 제약조건에 걸리는 중복을 DB가 한 문장 안에서 흡수하므로, 사전 존재 확인도 중복 예외 처리도
     * 필요하지 않다. 중복 요청은 기존 좋아요의 시점을 바꾸지 않는다. {@code created_date}가 좋아요한
     * 컬렉션 목록의 정렬·커서 기준이므로, 같은 요청을 반복했다고 목록 순서가 흔들리면 안 된다.
     * <p>
     * 이 호출이 실제로 행을 넣었는지는 알리지 않는다. MySQL이 돌려주는 갱신 행 수는 커넥터 설정
     * ({@code CLIENT_FOUND_ROWS})에 따라 달라지므로 그 값에 의미를 부여할 수 없고,
     * 등록 API는 어차피 "좋아요한 상태"라는 최종 상태만 보장한다.
     */
    void upsertLike(Long userId, Long collectionId);

    /**
     * 좋아요 취소. 지운 행 수를 돌려주므로 이미 취소된 좋아요를 다시 취소했는지 호출하는 쪽에서 알 수 있다.
     */
    long deleteLike(Long userId, Long collectionId);

    /**
     * 컬렉션 삭제 시 해당 컬렉션의 좋아요를 모두 지운다.
     */
    long deleteAllByCollectionId(Long collectionId);
}
