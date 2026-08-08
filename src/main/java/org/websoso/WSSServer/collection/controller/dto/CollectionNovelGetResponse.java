package org.websoso.WSSServer.collection.controller.dto;

/**
 * 컬렉션 상세에 표시하는 작품 하나. 작품 식별자만 주면 클라이언트가 작품 수만큼 상세를 다시 조회해야 하므로
 * 화면에 필요한 정보를 함께 담는다.
 */
public record CollectionNovelGetResponse(
        Long novelId,
        String title,
        String author,
        String novelImage,
        Boolean isCompleted,
        Float novelRating,
        Long novelRatingCount
) {
}
