package org.websoso.WSSServer.dto.auth;

public record ReissueResponse(
        String Authorization,
        String refreshToken
) {

    public static ReissueResponse of(String accessToken, String refreshToken) {
        return new ReissueResponse(accessToken, refreshToken);
    }
}
