package org.websoso.WSSServer.collection.repository.projection;

import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;

/**
 * 컬렉션 상세에 표시하는 작품 한 행. 목록 카드와 같은 작품 요약을 내보내므로 작품 테이블에서 읽는 값만 담고
 * 작품 통계는 조인하지 않는다.
 */
public record CollectionNovelRow(
        Long novelId,
        String title,
        String novelImage,
        String author
) {

    public CollectionNovelSummaryGetResponse toResponse() {
        return new CollectionNovelSummaryGetResponse(novelId, title, novelImage, author);
    }
}
