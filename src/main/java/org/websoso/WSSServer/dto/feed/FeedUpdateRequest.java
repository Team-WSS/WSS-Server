package org.websoso.WSSServer.dto.feed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record FeedUpdateRequest(
        @NotNull(message = "카테고리는 null일 수 없습니다.")
        @NotEmpty(message = "카테고리는 1개 이상 선택해야 합니다.")
        List<String> relevantCategories,

        @NotBlank(message = "피드 내용은 비어 있거나, 공백일 수 없습니다.")
        @Size(max = 2000, message = "피드 내용은 2000자를 초과할 수 없습니다.")
        String feedContent,

        Long novelId,

        @NotNull(message = "스포일러 여부는 null일 수 없습니다.")
        Boolean isSpoiler,

        Boolean isPublic
) {
}
