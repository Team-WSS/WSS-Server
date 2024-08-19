package org.websoso.WSSServer.oauth2.dto;

public record OAuth2UserDTO(
        String name,
        String email,
        String socialId
) {

    public static OAuth2UserDTO of(String name, String email, String socialId) {
        return new OAuth2UserDTO(
                name,
                email,
                socialId
        );
    }
}
