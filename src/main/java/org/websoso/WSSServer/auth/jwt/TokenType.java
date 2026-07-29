package org.websoso.WSSServer.auth.jwt;

import java.util.Arrays;

public enum TokenType {

    ACCESS("access"),
    REFRESH("refresh");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TokenType from(String value) {
        return Arrays.stream(values())
                .filter(tokenType -> tokenType.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
