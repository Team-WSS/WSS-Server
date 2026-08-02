package org.websoso.WSSServer.auth.controller.dto;

public record LogoutRequest(
        String refreshToken,
        String deviceIdentifier
) {
}
