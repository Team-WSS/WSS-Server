package org.websoso.WSSServer.collection.controller.dto;

import java.util.List;

/**
 * 컬렉션 상세 응답.
 * <p>
 * 비로그인 사용자도 공유 링크로 공개 컬렉션을 열 수 있으므로 소유자 정보를 함께 준다.
 * 비로그인 조회에서 {@code isMyCollection}은 항상 {@code false}다.
 */
public record CollectionGetResponse(
        Long collectionId,
        String collectionName,
        String collectionDescription,
        Boolean isPublic,
        Boolean isMyCollection,
        Long userId,
        String nickname,
        String avatarImage,
        Long representativeNovelId,
        int novelCount,
        List<CollectionNovelGetResponse> novels
) {
}
