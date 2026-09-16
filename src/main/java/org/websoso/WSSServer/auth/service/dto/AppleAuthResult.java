package org.websoso.WSSServer.auth.service.dto;

import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;
/**
 * Apple 인증(ID Token 검증 + Authorization Code 교환) 결과를 Application 계층에 전달하는 내부 DTO이다.
 */
public record AppleAuthResult(
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String userIdentifier,
        @SensitiveData(MaskingPolicy.EMAIL)
        String email,
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String appleRefreshToken
) {

    public static AppleAuthResult of(String userIdentifier, String email, String appleRefreshToken) {
        return new AppleAuthResult(userIdentifier, email, appleRefreshToken);
    }
}
