package org.websoso.WSSServer.dto.userNovel;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserNovelCreateRequest(
        @NotNull(message = "작품 id는 null일 수 없습니다.")
        Long novelId,
        //userNovelRating은 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0 중 하나만 가능하도록
        Float userNovelRating,
        @NotNull(message = "읽기 상태는 null일 수 없습니다.")
        //status는 WATCHING, WATCHED, QUIT 중에 하나만 가능하도록
        String status,
        //포맷정해주기
        String startDate,
        //포맷정해주기
        String endDate,
        //attractivePoints가 없는 경우 null이 아닌 빈배열로 받도록
        List<String> attractivePoints,
        //keywords가 없는 경우 null이 아닌 빈배열로 받도록
        List<String> keywords
) {
}
