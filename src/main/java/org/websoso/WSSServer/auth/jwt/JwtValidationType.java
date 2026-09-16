package org.websoso.WSSServer.auth.jwt;

public enum JwtValidationType {
    VALID_ACCESS,
    VALID_REFRESH,
    INVALID_TOKEN,
    INVALID_SIGNATURE,
    EXPIRED_ACCESS,
    EXPIRED_REFRESH,
    UNSUPPORTED_TOKEN,
    EMPTY_TOKEN,
    UNSUPPORTED_SUBJECT
}
