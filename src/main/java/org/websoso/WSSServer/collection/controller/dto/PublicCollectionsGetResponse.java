package org.websoso.WSSServer.collection.controller.dto;

import java.util.List;

/**
 * 전체 공개 컬렉션 목록 응답.
 * <p>
 * 전체 개수는 화면이 쓰지 않으므로 세지 않고 내보내지 않는다. {@code nextCursor}는 {@code hasNext}가
 * {@code false}이면 {@code null}이다.
 */
public record PublicCollectionsGetResponse(
        boolean hasNext,
        String nextCursor,
        List<PublicCollectionPreviewGetResponse> collections
) {

    public static PublicCollectionsGetResponse of(boolean hasNext, String nextCursor,
                                                  List<PublicCollectionPreviewGetResponse> collections) {
        return new PublicCollectionsGetResponse(hasNext, nextCursor, collections);
    }
}
