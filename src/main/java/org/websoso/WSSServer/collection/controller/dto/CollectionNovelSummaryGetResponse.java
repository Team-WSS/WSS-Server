package org.websoso.WSSServer.collection.controller.dto;

/**
 * 컬렉션 조회 응답이 공유하는 작품 요약. 목록의 대표 작품과 최근 추가 작품, 상세의 포함 작품이 모두 이 구조를 쓴다.
 * <p>
 * 클라이언트가 작품 수만큼 상세를 다시 조회하지 않도록 작품 카드를 그리는 데 필요한 값만 담는다.
 * 완결 여부와 평점처럼 컬렉션 화면이 쓰지 않는 값은 담지 않는다. 필요해지면 작품 상세 API에서 읽는다.
 */
public record CollectionNovelSummaryGetResponse(
        Long novelId,
        String title,
        String novelImage,
        String author
) {
}
