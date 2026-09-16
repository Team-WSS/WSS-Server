package org.websoso.WSSServer.collection.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 컬렉션 생성 요청.
 * <p>
 * {@code isPublic}은 생략할 수 있고 생략하면 서버가 공개(true)로 생성한다.
 * 작품 수·중복·대표 작품 포함 여부는 컬렉션 도메인 예외(COLLECTION-002/003/004)로 처리하기 위해
 * 여기서 검증하지 않는다.
 */
public record CollectionCreateRequest(
        @NotBlank(message = "컬렉션 이름은 비어 있거나, 공백일 수 없습니다.")
        @Size(max = 20, message = "컬렉션 이름은 20자를 초과할 수 없습니다.")
        String name,

        @Size(max = 60, message = "컬렉션 설명은 60자를 초과할 수 없습니다.")
        String description,

        Boolean isPublic,

        @NotNull(message = "컬렉션에 포함할 작품 목록은 null일 수 없습니다.")
        List<@NotNull(message = "컬렉션에 포함할 작품 ID는 null일 수 없습니다.") Long> novelIds,

        @NotNull(message = "대표 작품은 null일 수 없습니다.")
        Long representativeNovelId
) {
}
