package org.websoso.WSSServer.dto.auth;

public record LogoutRequest(
        String refreshToken
) {
}
