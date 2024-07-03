package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotNull;

public record EditProfileStatusRequest(
        @NotNull
        Boolean isProfilePublic
) {
}
