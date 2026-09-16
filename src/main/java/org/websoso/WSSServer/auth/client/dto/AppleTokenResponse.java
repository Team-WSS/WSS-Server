package org.websoso.WSSServer.auth.client.dto;

import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;
public record AppleTokenResponse(
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String access_token,
        String expires_in,
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String id_token,
        @SensitiveData(MaskingPolicy.CREDENTIAL)
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
