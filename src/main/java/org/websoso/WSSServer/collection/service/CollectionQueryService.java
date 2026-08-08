package org.websoso.WSSServer.collection.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelPreviewGetResponse;
import org.websoso.WSSServer.collection.domain.CollectionCursor;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.repository.CollectionQueryRepository;
import org.websoso.WSSServer.collection.repository.projection.CollectionDetailRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionNovelPreviewRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionNovelRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionPreviewRow;
import org.websoso.WSSServer.domain.common.SortCriteria;

/**
 * 컬렉션 조회 전용 Repository 접근을 캡슐화한다.
 * <p>
 * 유스케이스 전체의 트랜잭션 경계는 {@code CollectionFindApplication}이 소유한다. 이 클래스의 메서드는
 * 모두 조회 전용이므로 {@code readOnly} 트랜잭션 속성을 명시하되, 기본 전파 속성을 사용하므로
 * Application이 연 조회 트랜잭션이 있으면 거기에 참여한다.
 */
@Service
@RequiredArgsConstructor
public class CollectionQueryService {

    private final CollectionQueryRepository collectionQueryRepository;

    @Transactional(readOnly = true)
    public List<CollectionPreviewRow> findCollectionPreviewRows(Long ownerId, boolean includePrivate,
                                                                CollectionCursor cursor, int limit) {
        return collectionQueryRepository.findCollectionPreviewRows(ownerId, includePrivate, cursor, limit);
    }

    @Transactional(readOnly = true)
    public long countVisibleCollections(Long ownerId, boolean includePrivate) {
        return collectionQueryRepository.countVisibleCollections(ownerId, includePrivate);
    }

    /**
     * 여러 컬렉션의 최근 추가 작품 미리보기를 컬렉션별로 묶어 돌려준다.
     * 미리보기가 하나도 없는 컬렉션은 결과에 들어 있지 않으므로 호출하는 쪽에서 빈 목록으로 다룬다.
     */
    @Transactional(readOnly = true)
    public Map<Long, List<CollectionNovelPreviewGetResponse>> findRecentNovelPreviews(List<Long> collectionIds,
                                                                                      int previewSize) {
        return collectionQueryRepository.findRecentNovelPreviewRows(collectionIds, previewSize).stream()
                .collect(groupingBy(
                        CollectionNovelPreviewRow::collectionId,
                        LinkedHashMap::new,
                        mapping(CollectionNovelPreviewRow::toResponse, toList())
                ));
    }

    @Transactional(readOnly = true)
    public CollectionDetailRow getCollectionDetailRowOrException(Long collectionId) {
        return collectionQueryRepository.findCollectionDetailRow(collectionId)
                .orElseThrow(() -> new CustomCollectionException(
                        COLLECTION_NOT_FOUND,
                        "collection with the given id is not found"
                ));
    }

    @Transactional(readOnly = true)
    public List<CollectionNovelGetResponse> findCollectionNovels(Long collectionId, SortCriteria sortCriteria) {
        return collectionQueryRepository.findCollectionNovelRows(collectionId, sortCriteria).stream()
                .map(CollectionNovelRow::toResponse)
                .toList();
    }
}
