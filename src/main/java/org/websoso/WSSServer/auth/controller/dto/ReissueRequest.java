package org.websoso.WSSServer.auth.controller.dto;

import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;
public record ReissueRequest(
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String refreshToken
) {
}
