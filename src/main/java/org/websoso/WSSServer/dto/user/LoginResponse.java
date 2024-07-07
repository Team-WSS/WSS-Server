package org.websoso.WSSServer.dto.user;

public record LoginResponse(
        String Authorization
) {
    public static LoginResponse of(String token) {
        return new LoginResponse(token);
    }
}
