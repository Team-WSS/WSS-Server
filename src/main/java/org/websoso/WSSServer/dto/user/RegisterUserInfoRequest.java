package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotNull;
import org.websoso.WSSServer.validation.GenderConstraint;
import org.websoso.WSSServer.validation.NicknameConstraint;

public record RegisterUserInfoRequest(
        @NicknameConstraint
        String nickname,

        @NotNull
        @GenderConstraint
        String gender,

        Integer birth
) {
}
