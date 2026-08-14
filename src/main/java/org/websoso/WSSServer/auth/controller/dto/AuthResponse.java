package org.websoso.WSSServer.auth.controller.dto;

import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;
public record AuthResponse(
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String Authorization,
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String refreshToken,
        boolean isRegister
) {

    public static AuthResponse of(String Authorization, String refreshToken, boolean isRegister) {
        return new AuthResponse(Authorization, refreshToken, isRegister);
    }
}
