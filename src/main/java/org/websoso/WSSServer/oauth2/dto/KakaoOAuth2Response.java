package org.websoso.WSSServer.oauth2.dto;

import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KakaoOAuth2Response implements OAuth2Response {

    private final Map<String, Object> attributes;

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getName() {
        Map<String, String> properties = (Map<String, String>) attributes.get("properties");
        return properties.get("nickname");
    }

    @Override
    public String getEmail() {
        Map<String, String> kakaoAccount = (Map<String, String >) attributes.get("kakao_account");
        return kakaoAccount.get("email");
    }
}