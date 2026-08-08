package org.websoso.WSSServer.collection.controller.dto;

/**
 * 컬렉션 카드에 표지로 보여 주는 작품 정보. 카드는 표지 이미지만 필요하지만,
 * 접근성 대체 텍스트와 작품 이동을 위해 식별자와 제목을 함께 준다.
 */
public record CollectionNovelPreviewGetResponse(
        Long novelId,
        String title,
        String novelImage
) {
}
