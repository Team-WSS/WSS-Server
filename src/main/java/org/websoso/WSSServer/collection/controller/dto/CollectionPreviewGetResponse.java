package org.websoso.WSSServer.collection.controller.dto;

import java.util.List;

/**
 * 사용자별 컬렉션 목록의 컬렉션 카드 하나.
 * <p>
 * 대표 작품은 카드의 표지로, 최근 추가 작품은 카드 안의 미리보기 줄로 쓴다. 대표 작품이 최근 추가 작품에
 * 포함될 수도 있으므로 둘을 하나로 합치지 않고 따로 준다.
 */
public record CollectionPreviewGetResponse(
        Long collectionId,
        String collectionName,
        String collectionDescription,
        Boolean isPublic,
        Long novelCount,
        CollectionNovelPreviewGetResponse representativeNovel,
        List<CollectionNovelPreviewGetResponse> recentNovels
) {
}
