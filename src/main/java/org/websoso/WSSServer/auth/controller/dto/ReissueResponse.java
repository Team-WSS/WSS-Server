package org.websoso.WSSServer.auth.controller.dto;

import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;
public record ReissueResponse(
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String Authorization,
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String refreshToken
) {

    public static ReissueResponse of(String accessToken, String refreshToken) {
        return new ReissueResponse(accessToken, refreshToken);
    }
}
