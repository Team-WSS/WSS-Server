package org.websoso.WSSServer.collection.repository.projection;

import java.util.List;
import org.websoso.WSSServer.collection.controller.dto.CollectionGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelGetResponse;

/**
 * 컬렉션 상세의 컬렉션 자체 정보 한 행. 공개 여부와 소유자는 접근 정책 판단에 쓰이므로
 * 포함 작품을 읽기 전에 먼저 조회한다.
 */
public record CollectionDetailRow(
        Long collectionId,
        String name,
        String description,
        Boolean isPublic,
        Long ownerId,
        String ownerNickname,
        String ownerAvatarImage,
        Long representativeNovelId
) {

    public boolean isOwnedBy(Long userId) {
        return userId != null && userId.equals(ownerId);
    }

    public CollectionGetResponse toResponse(Long viewerId, List<CollectionNovelGetResponse> novels) {
        return new CollectionGetResponse(
                collectionId,
                name,
                description,
                isPublic,
                isOwnedBy(viewerId),
                ownerId,
                ownerNickname,
                ownerAvatarImage,
                representativeNovelId,
                novels.size(),
                novels
        );
    }
}
