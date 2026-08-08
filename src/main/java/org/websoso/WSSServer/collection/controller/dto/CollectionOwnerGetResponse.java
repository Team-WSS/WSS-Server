package org.websoso.WSSServer.collection.controller.dto;

/**
 * 컬렉션 상세의 소유자 정보. 공유 링크로 들어온 조회자에게 누가 만든 컬렉션인지 보여 주기 위해 함께 준다.
 * <p>
 * 컬렉션 자체의 값과 섞이지 않도록 평면 필드가 아니라 하나의 객체로 묶는다.
 * 아바타는 모든 사용자가 반드시 가지므로 {@code avatarImage}는 비어 있지 않다.
 */
public record CollectionOwnerGetResponse(
        Long userId,
        String nickname,
        String avatarImage
) {
}
