package org.websoso.WSSServer.collection.repository.projection;

import org.websoso.WSSServer.collection.controller.dto.CollectionNovelPreviewGetResponse;

/**
 * 컬렉션 카드의 최근 추가 작품 한 행. 어느 컬렉션의 미리보기인지 묶어야 하므로 컬렉션 식별자를 함께 읽는다.
 */
public record CollectionNovelPreviewRow(
        Long collectionId,
        Long novelId,
        String title,
        String novelImage
) {

    public CollectionNovelPreviewGetResponse toResponse() {
        return new CollectionNovelPreviewGetResponse(novelId, title, novelImage);
    }
}
