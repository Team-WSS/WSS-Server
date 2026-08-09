package org.websoso.WSSServer.collection.application;

import static org.websoso.WSSServer.collection.application.CollectionFindApplication.MAX_PAGE_SIZE;
import static org.websoso.WSSServer.collection.application.CollectionFindApplication.MIN_PAGE_SIZE;
import static org.websoso.WSSServer.collection.application.CollectionFindApplication.NOVEL_PREVIEW_SIZE;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_PAGE_SIZE;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;
import org.websoso.WSSServer.collection.controller.dto.LikedCollectionPreviewGetResponse;
import org.websoso.WSSServer.collection.controller.dto.LikedCollectionsGetResponse;
import org.websoso.WSSServer.collection.domain.CollectionLikeCursor;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.repository.projection.LikedCollectionRow;
import org.websoso.WSSServer.collection.service.CollectionLikeQueryService;
import org.websoso.WSSServer.collection.service.CollectionQueryService;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

/**
 * 좋아요한 컬렉션 목록 조회 유스케이스. 조회만 하므로 트랜잭션 경계는 읽기 전용이다.
 * <p>
 * 좋아요 데이터는 컬렉션이 비공개로 바뀌어도 지우지 않으므로, 지금 볼 수 있는지는 저장 시점이 아니라
 * 조회 시점에 판단한다. 본인이 만든 비공개 컬렉션은 그대로 노출하고, 다른 사용자의 비공개 컬렉션과
 * 차단 관계 사용자의 컬렉션만 숨긴다.
 */
@Service
@RequiredArgsConstructor
public class CollectionLikeFindApplication {

    private final BlockService blockService;
    private final CollectionLikeQueryService collectionLikeQueryService;
    private final CollectionQueryService collectionQueryService;

    @Transactional(readOnly = true)
    public LikedCollectionsGetResponse getLikedCollections(User viewer, String cursor, int size) {

        // 1. 페이지 크기를 확인한다.
        validatePageSize(size);

        // 2. 어느 방향이든 차단 관계인 사용자를 한 번에 읽어 목록에서 그 사용자의 컬렉션만 걸러 낸다.
        //    여러 사용자의 컬렉션이 섞이는 목록이므로 사용자별 컬렉션 목록과 달리 목록 자체를 거부하지 않는다.
        Long viewerId = viewer.getUserId();
        List<Long> blockedUserIds = blockService.findBlockRelationUserIds(viewerId);

        // 3. 다음 페이지가 있는지 알기 위해 요청 크기보다 하나 더 읽는다.
        List<LikedCollectionRow> rows = collectionLikeQueryService.findLikedCollectionRows(
                viewerId,
                blockedUserIds,
                decodeCursor(cursor),
                size + 1
        );
        boolean hasNext = rows.size() > size;
        List<LikedCollectionRow> pageRows = hasNext ? rows.subList(0, size) : rows;

        // 4. 이번 페이지 컬렉션의 최근 추가 작품을 한 번의 조회로 모두 가져온다.
        Map<Long, List<CollectionNovelSummaryGetResponse>> recentNovels = collectionQueryService
                .findRecentNovelPreviews(toCollectionIds(pageRows), NOVEL_PREVIEW_SIZE);

        List<LikedCollectionPreviewGetResponse> collections = pageRows.stream()
                .map(row -> row.toResponse(recentNovels.getOrDefault(row.collectionId(), List.of())))
                .toList();

        return LikedCollectionsGetResponse.of(
                collectionLikeQueryService.countLikedCollections(viewerId, blockedUserIds),
                hasNext,
                nextCursor(pageRows, hasNext),
                collections
        );
    }

    private void validatePageSize(int size) {
        if (size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            throw new CustomCollectionException(
                    INVALID_COLLECTION_PAGE_SIZE,
                    "collection page size must be between " + MIN_PAGE_SIZE + " and " + MAX_PAGE_SIZE
            );
        }
    }

    /**
     * 첫 페이지는 커서 없이 요청한다. 값이 있으면 이 API가 발급한 커서여야 하며, 아니면 요청 오류로 처리한다.
     */
    private CollectionLikeCursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        return CollectionLikeCursor.decode(cursor);
    }

    /**
     * 다음 페이지가 없으면 커서를 주지 않는다. {@code hasNext}와 커서 유무를 항상 일치시킨다.
     */
    private String nextCursor(List<LikedCollectionRow> pageRows, boolean hasNext) {
        if (!hasNext || pageRows.isEmpty()) {
            return null;
        }

        return pageRows.get(pageRows.size() - 1).toCursor().encode();
    }

    private List<Long> toCollectionIds(List<LikedCollectionRow> rows) {
        return rows.stream()
                .map(LikedCollectionRow::collectionId)
                .toList();
    }
}
