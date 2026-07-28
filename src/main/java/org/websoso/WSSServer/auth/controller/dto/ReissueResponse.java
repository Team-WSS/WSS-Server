package org.websoso.WSSServer.auth.controller.dto;

public record ReissueResponse(
        String Authorization,
        String refreshToken
) {

    public static ReissueResponse of(String accessToken, String refreshToken) {
        return new ReissueResponse(accessToken, refreshToken);
    }
}
