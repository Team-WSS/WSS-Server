package org.websoso.WSSServer.collection.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 컬렉션 전체 수정 요청.
 * <p>
 * 생성과 달리 {@code isPublic}을 필수로 받는다. 생략을 공개(true)로 해석하면 비공개 컬렉션을
 * 수정할 때 필드를 빠뜨리는 것만으로 컬렉션이 조용히 공개로 바뀌기 때문이다.
 */
public record CollectionUpdateRequest(
        @NotBlank(message = "컬렉션 이름은 비어 있거나, 공백일 수 없습니다.")
        @Size(max = 20, message = "컬렉션 이름은 20자를 초과할 수 없습니다.")
        String name,

        @Size(max = 60, message = "컬렉션 설명은 60자를 초과할 수 없습니다.")
        String description,

        @NotNull(message = "공개 여부는 null일 수 없습니다.")
        Boolean isPublic,

        @NotNull(message = "컬렉션에 포함할 작품 목록은 null일 수 없습니다.")
        List<@NotNull(message = "컬렉션에 포함할 작품 ID는 null일 수 없습니다.") Long> novelIds,

        @NotNull(message = "대표 작품은 null일 수 없습니다.")
        Long representativeNovelId
) {
}
