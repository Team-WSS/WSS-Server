package org.websoso.WSSServer.auth.controller.dto;

public record AuthResponse(
        String Authorization,
        String refreshToken,
        boolean isRegister
) {

    public static AuthResponse of(String Authorization, String refreshToken, boolean isRegister) {
        return new AuthResponse(Authorization, refreshToken, isRegister);
    }
}
