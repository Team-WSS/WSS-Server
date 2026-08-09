package org.websoso.WSSServer.collection.controller.dto;

import java.util.List;

/**
 * 사용자별 컬렉션 목록의 컬렉션 카드 하나.
 * <p>
 * 대표 작품은 카드의 표지로, 미리보기 작품은 카드 안의 미리보기 줄로 쓴다. 두 값은 서로 독립적이며
 * 대표 작품이 미리보기 작품에 그대로 포함될 수도 있으므로 둘을 하나로 합치지 않고 따로 준다.
 * {@code recentNovels}는 클라이언트가 정한 표시 순서 앞에서부터 최대 5개다.
 * <p>
 * 두 값 모두 상세의 포함 작품과 같은 {@link CollectionNovelSummaryGetResponse} 구조를 쓴다.
 */
public record CollectionPreviewGetResponse(
        Long collectionId,
        String collectionName,
        String collectionDescription,
        Boolean isPublic,
        Long novelCount,
        CollectionNovelSummaryGetResponse representativeNovel,
        List<CollectionNovelSummaryGetResponse> recentNovels
) {
}
