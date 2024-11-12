package org.websoso.WSSServer.dto.auth;

import static org.websoso.WSSServer.exception.error.CustomAppleLoginError.INVALID_APPLE_KEY;

import java.util.List;
import org.websoso.WSSServer.exception.exception.CustomAppleLoginException;

public record ApplePublicKeys(
        List<ApplePublicKey> keys
) {

    public ApplePublicKey getMatchingKey(final String alg, final String kid) {
        return keys.stream()
                .filter(key -> key.isSameAlg(alg) && key.isSameKid(kid))
                .findFirst()
                .orElseThrow(() -> new CustomAppleLoginException(INVALID_APPLE_KEY, "invalid apple key"));
    }
}
