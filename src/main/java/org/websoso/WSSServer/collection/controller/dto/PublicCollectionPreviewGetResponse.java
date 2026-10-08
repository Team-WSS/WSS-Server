package org.websoso.WSSServer.collection.controller.dto;

import java.util.List;

/**
 * 전체 공개 컬렉션 목록의 컬렉션 카드 하나. 홈의 컬렉션 섹션과 전체 컬렉션 화면이 함께 쓴다.
 * <p>
 * 여러 사용자의 컬렉션이 섞이는 목록이므로 카드마다 작성자({@code owner})를 함께 준다. 작성자 구조는 상세의
 * {@link CollectionOwnerGetResponse}를 그대로 쓰므로 닉네임 로그 마스킹 규칙도 같다.
 * <p>
 * {@code recentNovels}는 클라이언트가 정한 표시 순서 앞에서부터 최대 5개이며, 대표 작품을 따로 앞에 넣거나 빼지
 * 않는다. 실제 작품만 담으므로 5개보다 적을 수 있다. {@code novelCount}는 미리보기 개수가 아니라 전체 포함 작품 수다.
 * <p>
 * 공개 컬렉션만 담기므로 공개 여부를, 홈·전체 화면이 표시하지 않으므로 대표 작품과 좋아요 수를 내보내지 않는다.
 */
public record PublicCollectionPreviewGetResponse(
        Long collectionId,
        String collectionName,
        String collectionDescription,
        Long novelCount,
        CollectionOwnerGetResponse owner,
        List<CollectionNovelSummaryGetResponse> recentNovels
) {
}
