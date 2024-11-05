package org.websoso.WSSServer.config.jwt;

public enum JwtValidationType {
    VALID_ACCESS,
    VALID_REFRESH,
    INVALID_TOKEN,
    EXPIRED_ACCESS,
    EXPIRED_REFRESH,
    UNSUPPORTED_TOKEN,
    INACTIVE_TOKEN,
    EMPTY_TOKEN
}
