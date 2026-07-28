package org.websoso.WSSServer.auth.client.dto;

public record AppleTokenResponse(
        String access_token,
        String expires_in,
        String id_token,
        String refresh_token,
        String token_type,
        String error
) {

    public String getAccessToken() {
        return access_token;
    }

    public String getIdToken() {
        return id_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }
}
