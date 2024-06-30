package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotNull;

public record EditProfileStatusResponse(
        @NotNull
        Boolean isProfilePublic
) {
}
