package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotNull;
import org.websoso.WSSServer.validation.BirthConstraint;
import org.websoso.WSSServer.validation.GenderConstraint;

public record EditMyInfoRequest(
        @NotNull
        @GenderConstraint
        String gender,

        @NotNull(message = "출생연도는 null일 수 없습니다.")
        @BirthConstraint
        Integer birth
) {
}
