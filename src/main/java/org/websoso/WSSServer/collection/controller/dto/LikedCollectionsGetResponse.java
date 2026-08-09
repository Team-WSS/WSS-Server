package org.websoso.WSSServer.collection.controller.dto;

import java.util.List;

/**
 * 좋아요한 컬렉션 목록 응답.
 * <p>
 * {@code collectionsCount}는 이번 페이지 개수가 아니라 조회자가 좋아요한 컬렉션 중 지금 볼 수 있는 전체 개수다.
 * 좋아요는 남아 있지만 숨겨진 컬렉션(다른 사용자의 비공개 컬렉션, 차단 관계 사용자의 컬렉션)은 세지 않으므로
 * 목록에 보이는 개수와 항상 일치한다.
 * <p>
 * {@code nextCursor}는 더 가져올 컬렉션이 없으면 {@code null}이다.
 */
public record LikedCollectionsGetResponse(
        long collectionsCount,
        boolean hasNext,
        String nextCursor,
        List<LikedCollectionPreviewGetResponse> collections
) {

    public static LikedCollectionsGetResponse of(long collectionsCount, boolean hasNext, String nextCursor,
                                                 List<LikedCollectionPreviewGetResponse> collections) {
        return new LikedCollectionsGetResponse(collectionsCount, hasNext, nextCursor, collections);
    }
}
