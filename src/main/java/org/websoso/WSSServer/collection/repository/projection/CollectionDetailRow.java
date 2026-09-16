package org.websoso.WSSServer.collection.repository.projection;

import java.util.List;
import org.websoso.WSSServer.collection.controller.dto.CollectionGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionOwnerGetResponse;

/**
 * 컬렉션 상세의 컬렉션 자체 정보 한 행. 공개 여부와 소유자는 접근 정책 판단에 쓰이므로
 * 포함 작품을 읽기 전에 먼저 조회한다.
 * <p>
 * 아바타는 모든 사용자가 반드시 가지므로 {@code ownerAvatarImage}는 비어 있지 않다.
 */
public record CollectionDetailRow(
        Long collectionId,
        String name,
        String description,
        Boolean isPublic,
        Long ownerId,
        String ownerNickname,
        String ownerAvatarImage,
        Long representativeNovelId,
        Long likeCount
) {

    public boolean isOwnedBy(Long userId) {
        return userId != null && userId.equals(ownerId);
    }

    /**
     * {@code isLiked}는 조회자가 이 컬렉션에 좋아요를 눌렀는지다. 컬렉션 자체의 값이 아니라 조회자별 값이므로
     * 컬렉션을 읽는 이 행이 아니라 호출하는 쪽이 판단해서 넘긴다. 비로그인 조회에서는 항상 {@code false}다.
     */
    public CollectionGetResponse toResponse(Long viewerId, boolean isLiked,
                                            List<CollectionNovelSummaryGetResponse> novels) {
        return new CollectionGetResponse(
                collectionId,
                name,
                description,
                isPublic,
                isOwnedBy(viewerId),
                toOwner(),
                representativeNovelId,
                novels.size(),
                likeCount == null ? 0L : likeCount,
                isLiked,
                novels
        );
    }

    private CollectionOwnerGetResponse toOwner() {
        return new CollectionOwnerGetResponse(ownerId, ownerNickname, ownerAvatarImage);
    }
}
