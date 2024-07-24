package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.hibernate.validator.constraints.Range;
import org.websoso.WSSServer.validation.GenderConstraint;
import org.websoso.WSSServer.validation.NicknameConstraint;

public record RegisterUserInfoRequest(
        @NicknameConstraint
        String nickname,

        @NotNull
        @GenderConstraint
        String gender,

        @NotNull(message = "출생연도는 null일 수 없습니다.")
        @Range(min = 1900, max = 2024, message = "출생연도는 1900 ~ 2024 사이여야 합니다.")
        Integer birth,
        //TODO 날짜 상수화, 동적으로 가져와야 함

        @NotNull(message = "선호 장르는 null일 수 없습니다.")
        List<String> genrePreferences
) {
}
