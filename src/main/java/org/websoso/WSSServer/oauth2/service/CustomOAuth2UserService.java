package org.websoso.WSSServer.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.oauth2.dto.CustomOauth2User;
import org.websoso.WSSServer.oauth2.dto.KakaoOauth2Response;
import org.websoso.WSSServer.oauth2.dto.OAuth2Response;
import org.websoso.WSSServer.oauth2.dto.OAuth2UserDTO;
import org.websoso.WSSServer.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoOauth2Response(oAuth2User.getAttributes());
        }

        String socialId = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
        User existedUserOrNull = userRepository.findBySocialId(socialId);
        if (existedUserOrNull == null) {
            userRepository.save(User.createBySocial(socialId, oAuth2Response.getName()));
            OAuth2UserDTO oAuth2UserDTO = OAuth2UserDTO.of(oAuth2Response.getName(), socialId);
            return new CustomOauth2User(oAuth2UserDTO);
        } else {
            OAuth2UserDTO oAuth2UserDTO
                    = OAuth2UserDTO.of(existedUserOrNull.getNickname(), existedUserOrNull.getSocialId());
            return new CustomOauth2User(oAuth2UserDTO);
        }
    }
}
