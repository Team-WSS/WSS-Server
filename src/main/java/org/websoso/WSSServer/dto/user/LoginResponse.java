package org.websoso.WSSServer.dto.user;

import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;
public record LoginResponse(
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String Authorization
) {
    public static LoginResponse of(String token) {
        return new LoginResponse(token);
    }
}
