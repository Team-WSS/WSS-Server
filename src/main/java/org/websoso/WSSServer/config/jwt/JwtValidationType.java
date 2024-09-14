package org.websoso.WSSServer.config.jwt;

public enum JwtValidationType {
    VALID_TOKEN,
    INVALID_SIGNATURE,
    INVALID_TOKEN,
    EXPIRED_TOKEN,
    UNSUPPORTED_TOKEN,
    EMPTY_TOKEN
}
