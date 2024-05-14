package org.websoso.WSSServer.dto.User;

public record LoginResponse(
        String Authorization
) {
    public static LoginResponse of(String token) {
        return new LoginResponse(token);
    }
}
