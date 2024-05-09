package org.websoso.WSSServer.dto.feed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record FeedCreateRequest(
        @NotNull @NotEmpty
        List<String> relevantCategories,
        @NotBlank @Size(min = 1, max = 2000)
        String feedContent,
        Long novelId,
        @NotNull
        Boolean isSpoiler
) {}