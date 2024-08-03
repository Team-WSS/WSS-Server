package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.websoso.WSSServer.validation.BirthConstraint;
import org.websoso.WSSServer.validation.GenderConstraint;
import org.websoso.WSSServer.validation.NicknameConstraint;

public record RegisterUserInfoRequest(
        @NicknameConstraint
        String nickname,

        @NotNull
        @GenderConstraint
        String gender,

        @NotNull(message = "출생연도는 null일 수 없습니다.")
        @BirthConstraint
        Integer birth,

        @NotNull(message = "선호 장르는 null일 수 없습니다.")
        List<String> genrePreferences
) {
}
