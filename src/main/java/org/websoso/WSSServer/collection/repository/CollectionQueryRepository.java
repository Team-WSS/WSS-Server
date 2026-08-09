package org.websoso.WSSServer.collection.repository;

import java.util.List;
import java.util.Optional;
import org.websoso.WSSServer.collection.domain.CollectionCursor;
import org.websoso.WSSServer.collection.repository.projection.CollectionDetailRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionNovelPreviewRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionNovelRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionPreviewRow;
import org.websoso.WSSServer.domain.common.SortCriteria;

/**
 * 컬렉션 조회 전용 쿼리. 엔티티를 그대로 돌려주지 않고 화면에 필요한 값만 projection으로 읽는다.
 */
public interface CollectionQueryRepository {

    /**
     * 사용자별 컬렉션 목록 한 페이지를 최초 생성 시점 내림차순으로 읽는다.
     * 대표 작품은 같은 쿼리에서 조인해 읽으므로 컬렉션 수만큼 추가 조회가 생기지 않는다.
     *
     * @param includePrivate 비공개 컬렉션까지 포함할지 여부. 본인 조회일 때만 {@code true}다.
     * @param cursor         이전 페이지의 마지막 컬렉션. 첫 페이지는 {@code null}이다.
     * @param limit          읽을 최대 행 수. 다음 페이지 존재 여부를 판단하려면 요청 크기보다 하나 더 읽는다.
     */
    List<CollectionPreviewRow> findCollectionPreviewRows(Long ownerId, boolean includePrivate,
                                                         CollectionCursor cursor, int limit);

    /**
     * 조회자가 볼 수 있는 전체 컬렉션 개수를 센다. 페이지 크기와 무관하므로 마이페이지가 {@code size=3}으로
     * 호출해도 전체 개수를 그대로 표시할 수 있다.
     */
    long countVisibleCollections(Long ownerId, boolean includePrivate);

    /**
     * 여러 컬렉션의 최근 추가 작품을 컬렉션당 {@code previewSize}개까지 한 번의 쿼리로 읽는다.
     * 컬렉션마다 따로 조회하지 않으므로 목록 크기에 비례해 쿼리가 늘지 않는다.
     */
    List<CollectionNovelPreviewRow> findRecentNovelPreviewRows(List<Long> collectionIds, int previewSize);

    /**
     * 컬렉션 상세의 컬렉션 자체 정보를 읽는다. 공개 여부와 소유자가 포함되므로 포함 작품을 읽기 전에
     * 접근 정책을 판단할 수 있다.
     * <p>
     * 소유자의 아바타는 도메인상 필수이므로 아바타를 inner join으로 읽는다. outer join으로 읽으면
     * 아바타가 없는 사용자를 정상으로 취급해 응답의 {@code owner.avatarImage}가 조용히 비게 된다.
     */
    Optional<CollectionDetailRow> findCollectionDetailRow(Long collectionId);

    /**
     * 컬렉션에 포함된 작품을 컬렉션에 추가된 시점 기준으로 정렬해 읽는다.
     * 같은 시각에 추가된 작품이 있어도 순서가 흔들리지 않도록 식별자를 보조 정렬 기준으로 쓴다.
     * <p>
     * 목록 카드와 같은 작품 요약만 내보내므로 작품 통계는 읽지 않는다.
     */
    List<CollectionNovelRow> findCollectionNovelRows(Long collectionId, SortCriteria sortCriteria);
}
