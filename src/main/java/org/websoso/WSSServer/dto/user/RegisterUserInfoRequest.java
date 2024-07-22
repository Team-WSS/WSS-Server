package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotNull;
import java.time.Year;
import org.websoso.WSSServer.validation.GenderConstraint;

public record RegisterUserInfoRequest(
        String nickname,
        @NotNull
        @GenderConstraint
        String gender,
        Year birth
) {
}
