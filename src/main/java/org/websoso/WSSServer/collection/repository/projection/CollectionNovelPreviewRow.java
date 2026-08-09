package org.websoso.WSSServer.collection.repository.projection;

import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;

/**
 * 컬렉션 카드의 미리보기 작품 한 행. 여러 컬렉션의 미리보기를 한 번에 읽으므로 어느 컬렉션의 미리보기인지
 * 묶기 위해 컬렉션 식별자를 함께 읽는다. 내보내는 작품 요약은 상세의 포함 작품과 같다.
 */
public record CollectionNovelPreviewRow(
        Long collectionId,
        Long novelId,
        String title,
        String novelImage,
        String author
) {

    public CollectionNovelSummaryGetResponse toResponse() {
        return new CollectionNovelSummaryGetResponse(novelId, title, novelImage, author);
    }
}
