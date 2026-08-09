package org.websoso.WSSServer.collection.repository.projection;

import java.time.LocalDateTime;
import java.util.List;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;
import org.websoso.WSSServer.collection.controller.dto.LikedCollectionPreviewGetResponse;
import org.websoso.WSSServer.collection.domain.CollectionLikeCursor;

/**
 * 좋아요한 컬렉션 목록 한 행.
 * <p>
 * 목록이 좋아요를 누른 순서로 정렬되므로 커서를 만들려면 응답에 내보내지 않는 좋아요 식별자와
 * 좋아요 시점이 필요하다. 대표 작품은 목록 조회에서 함께 조인해 읽으므로 컬렉션 수만큼 추가 조회가 생기지 않는다.
 */
public record LikedCollectionRow(
        Long collectionLikeId,
        LocalDateTime likedDate,
        Long collectionId,
        String name,
        String description,
        Boolean isPublic,
        Long novelCount,
        Long likeCount,
        Long representativeNovelId,
        String representativeNovelTitle,
        String representativeNovelImage,
        String representativeNovelAuthor
) {

    public CollectionLikeCursor toCursor() {
        return CollectionLikeCursor.of(likedDate, collectionLikeId);
    }

    public LikedCollectionPreviewGetResponse toResponse(List<CollectionNovelSummaryGetResponse> recentNovels) {
        return new LikedCollectionPreviewGetResponse(
                collectionId,
                name,
                description,
                isPublic,
                novelCount == null ? 0L : novelCount,
                likeCount == null ? 0L : likeCount,
                toRepresentativeNovel(),
                recentNovels
        );
    }

    /**
     * 대표 작품은 컬렉션에 포함된 작품이므로 정상적으로는 항상 존재한다. 작품이 지워진 컬렉션까지 조회가
     * 실패하지 않도록 조인 결과가 비면 대표 작품 없이 카드를 돌려준다.
     */
    private CollectionNovelSummaryGetResponse toRepresentativeNovel() {
        if (representativeNovelId == null) {
            return null;
        }

        return new CollectionNovelSummaryGetResponse(
                representativeNovelId,
                representativeNovelTitle,
                representativeNovelImage,
                representativeNovelAuthor
        );
    }
}
