package org.websoso.WSSServer.collection.controller.dto;

import java.util.List;

/**
 * 좋아요한 컬렉션 목록의 컬렉션 카드 하나.
 * <p>
 * 사용자별 컬렉션 목록의 카드({@link CollectionPreviewGetResponse})와 같은 값을 담고 좋아요 수를 더한다.
 * 좋아요한 컬렉션 목록에 담긴 컬렉션은 모두 조회자가 좋아요한 컬렉션이므로 "내가 좋아요했는지"는 따로 주지 않는다.
 * <p>
 * {@code isPublic}이 {@code false}인 카드는 조회자 본인이 만든 비공개 컬렉션뿐이다.
 * 다른 사용자의 비공개 컬렉션은 목록에서 숨기기 때문이다.
 */
public record LikedCollectionPreviewGetResponse(
        Long collectionId,
        String collectionName,
        String collectionDescription,
        Boolean isPublic,
        Long novelCount,
        Long likeCount,
        CollectionNovelSummaryGetResponse representativeNovel,
        List<CollectionNovelSummaryGetResponse> recentNovels
) {
}
