package org.websoso.WSSServer.dto.auth;

public record ReissueResponse(
        String Authorization
) {

    public static ReissueResponse of(String accessToken) {
        return new ReissueResponse(accessToken);
    }
}
