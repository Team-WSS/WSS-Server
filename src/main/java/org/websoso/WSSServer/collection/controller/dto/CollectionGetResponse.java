package org.websoso.WSSServer.collection.controller.dto;

import java.util.List;

/**
 * 컬렉션 상세 응답.
 * <p>
 * 비로그인 사용자도 공유 링크로 공개 컬렉션을 열 수 있으므로 소유자 정보를 함께 준다.
 * 비로그인 조회에서 {@code isMyCollection}은 항상 {@code false}다.
 * <p>
 * 포함 작품은 목록의 대표 작품·미리보기 작품과 같은 {@link CollectionNovelSummaryGetResponse} 구조를 쓴다.
 * <p>
 * {@code likeCount}는 이 컬렉션이 받은 전체 좋아요 수이고 {@code isLiked}는 조회자가 좋아요를 눌렀는지다.
 * 좋아요 데이터는 컬렉션이 비공개로 바뀌어도 유지되므로 {@code likeCount}는 공개 여부와 무관하다.
 * 비로그인 조회에서 {@code isLiked}는 항상 {@code false}다.
 */
public record CollectionGetResponse(
        Long collectionId,
        String collectionName,
        String collectionDescription,
        Boolean isPublic,
        Boolean isMyCollection,
        CollectionOwnerGetResponse owner,
        Long representativeNovelId,
        int novelCount,
        Long likeCount,
        Boolean isLiked,
        List<CollectionNovelSummaryGetResponse> novels
) {
}
