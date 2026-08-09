package org.websoso.WSSServer.collection.controller.dto;

import java.util.List;

/**
 * 사용자별 컬렉션 목록 응답.
 * <p>
 * {@code collectionsCount}는 이번 페이지 개수가 아니라 요청한 조회자가 이 사용자에게서 볼 수 있는 전체
 * 컬렉션 개수다. 따라서 마이페이지가 {@code size=3}으로 호출해도 "컬렉션 N개"를 그대로 표시할 수 있다.
 * <p>
 * {@code nextCursor}는 더 가져올 컬렉션이 없으면 {@code null}이다.
 */
public record CollectionsGetResponse(
        long collectionsCount,
        boolean hasNext,
        String nextCursor,
        List<CollectionPreviewGetResponse> collections
) {

    public static CollectionsGetResponse of(long collectionsCount, boolean hasNext, String nextCursor,
                                            List<CollectionPreviewGetResponse> collections) {
        return new CollectionsGetResponse(collectionsCount, hasNext, nextCursor, collections);
    }
}
