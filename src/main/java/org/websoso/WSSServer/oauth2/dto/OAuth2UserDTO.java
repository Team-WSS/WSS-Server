package org.websoso.WSSServer.oauth2.dto;

public record OAuth2UserDTO(
        String name,
        String socialId
) {

    public static OAuth2UserDTO of(String name, String socialId) {
        return new OAuth2UserDTO(
                name,
                socialId
        );
    }
}
