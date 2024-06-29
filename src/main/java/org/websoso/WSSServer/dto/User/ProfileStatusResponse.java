package org.websoso.WSSServer.dto.user;

public record ProfileStatusResponse(
        boolean isProfilePublic
) {
    public static ProfileStatusResponse of(Boolean isProfilePublic) {
        return new ProfileStatusResponse(isProfilePublic);
    }
}
