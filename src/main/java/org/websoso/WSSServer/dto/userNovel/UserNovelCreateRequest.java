package org.websoso.WSSServer.dto.userNovel;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.hibernate.annotations.ColumnDefault;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.validation.UserNovelRatingConstraint;

public record UserNovelCreateRequest(
        @NotNull(message = "작품 id는 null일 수 없습니다.")
        Long novelId,
        @UserNovelRatingConstraint
        @ColumnDefault("0.0")
        Float userNovelRating,
        @NotNull(message = "읽기 상태는 null일 수 없습니다.")
        ReadStatus status,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "시작 날짜는 yyyy-MM-dd 형식이어야 합니다.")
        String startDate,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "종료 날짜는 yyyy-MM-dd 형식이어야 합니다.")
        String endDate,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @Size(max = 3, message = "매력 포인트는 최대 3개까지 가능합니다.")
        List<String> attractivePoints,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @Size(max = 10, message = "키워드는 최대 10개까지 가능합니다.")
        List<Integer> keywordIds
) {
}
