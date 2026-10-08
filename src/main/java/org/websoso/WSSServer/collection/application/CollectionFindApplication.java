package org.websoso.WSSServer.collection.application;

import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_PAGE_SIZE;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.PRIVATE_COLLECTION_ACCESS;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.collection.controller.dto.CollectionGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionPreviewGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionsGetResponse;
import org.websoso.WSSServer.collection.controller.dto.PublicCollectionPreviewGetResponse;
import org.websoso.WSSServer.collection.controller.dto.PublicCollectionsGetResponse;
import org.websoso.WSSServer.collection.domain.CollectionCursor;
import org.websoso.WSSServer.collection.domain.PublicCollectionCursor;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.repository.projection.CollectionDetailRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionPreviewRow;
import org.websoso.WSSServer.collection.repository.projection.PublicCollectionRow;
import org.websoso.WSSServer.collection.service.CollectionLikeService;
import org.websoso.WSSServer.collection.service.CollectionQueryService;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;
import org.websoso.WSSServer.user.service.UserService;

/**
 * 컬렉션 목록·상세 조회 유스케이스. 조회만 하므로 트랜잭션 경계는 모두 읽기 전용이다.
 */
@Service
@RequiredArgsConstructor
public class CollectionFindApplication {

    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 컬렉션 카드의 미리보기 줄에 보여 주는 작품 수.
     */
    public static final int NOVEL_PREVIEW_SIZE = 5;

    private final UserService userService;
    private final BlockService blockService;
    private final CollectionQueryService collectionQueryService;
    private final CollectionLikeService collectionLikeService;

    /**
     * 사용자별 컬렉션 목록을 커서 기반으로 조회한다. 마이페이지 미리보기는 이 API를 {@code size=3}으로 호출한다.
     */
    @Transactional(readOnly = true)
    public CollectionsGetResponse getUserCollections(User viewer, Long ownerId, String cursor, int size) {

        // 1. 페이지 크기를 확인한다.
        validatePageSize(size);

        // 2. 컬렉션 주인이 존재하는지 확인한다.
        Long viewerId = viewer.getUserId();
        userService.getUserOrException(ownerId);

        // 3. 어느 방향이든 차단 관계면 목록을 보여 주지 않는다.
        blockService.validateNotBlocked(viewerId, ownerId);

        // 4. 본인 목록에만 비공개 컬렉션을 포함한다.
        boolean includePrivate = viewerId.equals(ownerId);

        // 5. 다음 페이지가 있는지 알기 위해 요청 크기보다 하나 더 읽는다.
        List<CollectionPreviewRow> rows = collectionQueryService.findCollectionPreviewRows(
                ownerId,
                includePrivate,
                decodeCursor(cursor),
                size + 1
        );
        boolean hasNext = rows.size() > size;
        List<CollectionPreviewRow> pageRows = hasNext ? rows.subList(0, size) : rows;

        // 6. 이번 페이지 컬렉션의 미리보기 작품을 한 번의 조회로 모두 가져온다.
        Map<Long, List<CollectionNovelSummaryGetResponse>> recentNovels = collectionQueryService
                .findRecentNovelPreviews(toCollectionIds(pageRows), NOVEL_PREVIEW_SIZE);

        List<CollectionPreviewGetResponse> collections = pageRows.stream()
                .map(row -> row.toResponse(recentNovels.getOrDefault(row.collectionId(), List.of())))
                .toList();

        return CollectionsGetResponse.of(
                collectionQueryService.countVisibleCollections(ownerId, includePrivate),
                hasNext,
                nextCursor(pageRows, hasNext),
                collections
        );
    }

    /**
     * 모든 사용자의 공개 컬렉션을 최초 생성 시점 최신순으로 커서 기반 조회한다. 홈의 컬렉션 섹션과 전체 컬렉션
     * 화면이 함께 쓴다. 비로그인 조회를 허용하므로 {@code viewer}가 없을 수 있다.
     * <p>
     * 공개 여부와 차단 관계는 요청마다 그 시점의 값으로 판단한다. 여러 페이지에 걸친 고정 스냅샷은 두지 않는다.
     */
    @Transactional(readOnly = true)
    public PublicCollectionsGetResponse getPublicCollections(User viewer, String cursor, int size) {

        // 1. 페이지 크기와 커서를 확인한다. 둘 다 조회 전에 거부한다.
        validatePageSize(size);
        PublicCollectionCursor decodedCursor = decodePublicCursor(cursor);

        // 2. 로그인 조회자면 어느 방향이든 차단 관계인 사용자를 한 번에 읽어 그 사용자의 컬렉션만 걸러 낸다.
        //    여러 사용자의 컬렉션이 섞이는 목록이므로 목록 자체를 거부하지 않는다. 비로그인 조회에는 차단 관계가 없다.
        List<Long> blockedUserIds = viewer == null
                ? List.of()
                : blockService.findBlockRelationUserIds(viewer.getUserId());

        // 3. 공개·차단 조건을 적용한 뒤 요청 크기보다 하나 더 읽어 다음 페이지 존재 여부를 판단한다.
        //    본인의 비공개 컬렉션도 이 목록에는 포함하지 않는다.
        List<PublicCollectionRow> rows = collectionQueryService.findPublicCollectionRows(
                blockedUserIds,
                decodedCursor,
                size + 1
        );
        boolean hasNext = rows.size() > size;
        List<PublicCollectionRow> pageRows = hasNext ? rows.subList(0, size) : rows;

        // 4. 이번 페이지 컬렉션의 미리보기 작품을 한 번의 조회로 모두 가져온다.
        Map<Long, List<CollectionNovelSummaryGetResponse>> recentNovels = collectionQueryService
                .findRecentNovelPreviews(toPublicCollectionIds(pageRows), NOVEL_PREVIEW_SIZE);

        List<PublicCollectionPreviewGetResponse> collections = pageRows.stream()
                .map(row -> row.toResponse(recentNovels.getOrDefault(row.collectionId(), List.of())))
                .toList();

        return PublicCollectionsGetResponse.of(
                hasNext,
                nextPublicCursor(pageRows, hasNext),
                collections
        );
    }

    /**
     * 컬렉션 상세를 조회한다. 공유 링크로 들어온 비로그인 사용자도 공개 컬렉션은 볼 수 있으므로
     * {@code viewer}가 없을 수 있다.
     */
    @Transactional(readOnly = true)
    public CollectionGetResponse getCollection(User viewer, Long collectionId, SortCriteria sortCriteria) {

        // 1. 존재하는 컬렉션인지 확인하고 접근 정책 판단에 필요한 정보를 읽는다.
        Long viewerId = viewer == null ? null : viewer.getUserId();
        CollectionDetailRow detail = collectionQueryService.getCollectionDetailRowOrException(collectionId);

        // 2. 로그인 조회자와 소유자가 어느 방향이든 차단 관계면 상세를 보여 주지 않는다.
        //    비로그인 조회에는 차단 관계가 없으므로 이 검증은 통과한다.
        blockService.validateNotBlocked(viewerId, detail.ownerId());

        // 3. 비공개 컬렉션은 소유자만 볼 수 있다. 비로그인 조회자는 소유자가 될 수 없다.
        validateVisible(detail, viewerId);

        // 4. 포함 작품을 클라이언트가 저장해 둔 표시 순서 기준으로 정렬해 읽는다.
        List<CollectionNovelSummaryGetResponse> novels = collectionQueryService.findCollectionNovels(
                collectionId,
                sortCriteria
        );

        // 5. 좋아요 수는 컬렉션 자체의 값이라 상세 쿼리가 함께 읽고, 조회자가 좋아요했는지만 따로 확인한다.
        //    비로그인 조회는 좋아요를 누를 수 없으므로 조회 없이 false다.
        boolean isLiked = collectionLikeService.isLikedBy(viewerId, collectionId);

        return detail.toResponse(viewerId, isLiked, novels);
    }

    private void validatePageSize(int size) {
        if (size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            throw new CustomCollectionException(
                    INVALID_COLLECTION_PAGE_SIZE,
                    "collection page size must be between " + MIN_PAGE_SIZE + " and " + MAX_PAGE_SIZE
            );
        }
    }

    private void validateVisible(CollectionDetailRow detail, Long viewerId) {
        if (!detail.isPublic() && !detail.isOwnedBy(viewerId)) {
            throw new CustomCollectionException(
                    PRIVATE_COLLECTION_ACCESS,
                    "only the owner of the collection can read a private collection"
            );
        }
    }

    /**
     * 첫 페이지는 커서 없이 요청한다. 값이 있으면 이 API가 발급한 커서여야 하며, 아니면 요청 오류로 처리한다.
     */
    private CollectionCursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        return CollectionCursor.decode(cursor);
    }

    /**
     * 다음 페이지가 없으면 커서를 주지 않는다. 클라이언트가 마지막 페이지에서 같은 요청을 반복하지 않도록
     * {@code hasNext}와 커서 유무를 일치시킨다.
     */
    private String nextCursor(List<CollectionPreviewRow> pageRows, boolean hasNext) {
        if (!hasNext || pageRows.isEmpty()) {
            return null;
        }

        return pageRows.get(pageRows.size() - 1).toCursor().encode();
    }

    /**
     * 첫 페이지는 커서 없이 요청한다. 빈 문자열과 공백만 있는 값도 첫 페이지로 본다.
     * 값이 있으면 전체 공개 목록이 발급한 커서여야 하며, 다른 목록의 커서는 요청 오류로 처리한다.
     */
    private PublicCollectionCursor decodePublicCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        return PublicCollectionCursor.decode(cursor);
    }

    private String nextPublicCursor(List<PublicCollectionRow> pageRows, boolean hasNext) {
        if (!hasNext || pageRows.isEmpty()) {
            return null;
        }

        return pageRows.get(pageRows.size() - 1).toCursor().encode();
    }

    private List<Long> toPublicCollectionIds(List<PublicCollectionRow> rows) {
        return rows.stream()
                .map(PublicCollectionRow::collectionId)
                .toList();
    }

    private List<Long> toCollectionIds(List<CollectionPreviewRow> rows) {
        return rows.stream()
                .map(CollectionPreviewRow::collectionId)
                .toList();
    }
}
