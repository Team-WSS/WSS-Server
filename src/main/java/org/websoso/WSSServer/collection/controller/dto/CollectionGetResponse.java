package org.websoso.WSSServer.collection.controller.dto;

import java.util.List;

/**
 * 컬렉션 상세 응답.
 * <p>
 * 비로그인 사용자도 공유 링크로 공개 컬렉션을 열 수 있으므로 소유자 정보를 함께 준다.
 * 비로그인 조회에서 {@code isMyCollection}은 항상 {@code false}다.
 * <p>
 * 포함 작품은 목록의 대표 작품·최근 추가 작품과 같은 {@link CollectionNovelSummaryGetResponse} 구조를 쓴다.
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
        List<CollectionNovelSummaryGetResponse> novels
) {
}
