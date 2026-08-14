package org.websoso.WSSServer.auth.controller.dto;

import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;
public record LogoutRequest(
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String refreshToken,
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String deviceIdentifier
) {
}
