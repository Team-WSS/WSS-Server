package org.websoso.WSSServer.collection.repository.projection;

import java.time.LocalDateTime;
import java.util.List;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionOwnerGetResponse;
import org.websoso.WSSServer.collection.controller.dto.PublicCollectionPreviewGetResponse;
import org.websoso.WSSServer.collection.domain.PublicCollectionCursor;

/**
 * 전체 공개 컬렉션 목록 한 행. 커서를 만들려면 생성 시점이 필요하므로 응답에 내보내지 않는 {@code createdDate}도
 * 함께 읽는다. 작성자와 작성자 아바타는 목록 쿼리에서 함께 조인해 읽으므로 카드 수만큼 추가 조회가 생기지 않는다.
 * <p>
 * 탈퇴한 사용자의 컬렉션은 소유자가 알 수 없는 사용자로 넘어가 있으므로 작성자도 그 사용자의 프로필로 읽힌다.
 */
public record PublicCollectionRow(
        Long collectionId,
        String name,
        String description,
        LocalDateTime createdDate,
        Long novelCount,
        Long ownerId,
        String ownerNickname,
        String ownerAvatarImage
) {

    public PublicCollectionCursor toCursor() {
        return PublicCollectionCursor.of(createdDate, collectionId);
    }

    public PublicCollectionPreviewGetResponse toResponse(List<CollectionNovelSummaryGetResponse> recentNovels) {
        return new PublicCollectionPreviewGetResponse(
                collectionId,
                name,
                description,
                novelCount == null ? 0L : novelCount,
                new CollectionOwnerGetResponse(ownerId, ownerNickname, ownerAvatarImage),
                recentNovels
        );
    }
}
