package org.websoso.WSSServer.collection.repository.projection;

import java.math.BigDecimal;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelGetResponse;

/**
 * 컬렉션 상세에 표시하는 작품 한 행. 평점은 작품 통계에서 함께 읽는다.
 * 통계가 아직 없는 작품이 있을 수 있으므로 비어 있으면 0으로 내보낸다.
 */
public record CollectionNovelRow(
        Long novelId,
        String title,
        String author,
        String novelImage,
        Boolean isCompleted,
        BigDecimal averageRating,
        Long ratingCount
) {

    private static final float RATING_SCALE = 10.0f;

    public CollectionNovelGetResponse toResponse() {
        return new CollectionNovelGetResponse(
                novelId,
                title,
                author,
                novelImage,
                isCompleted,
                roundedRating(),
                ratingCount == null ? 0L : ratingCount
        );
    }

    /**
     * 기존 작품 요약 응답과 같은 방식으로 소수점 첫째 자리까지만 내보낸다.
     */
    private float roundedRating() {
        if (averageRating == null) {
            return 0.0f;
        }

        return Math.round(averageRating.floatValue() * RATING_SCALE) / RATING_SCALE;
    }
}
