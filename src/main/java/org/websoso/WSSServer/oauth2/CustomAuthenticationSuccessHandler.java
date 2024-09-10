package org.websoso.WSSServer.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.oauth2.dto.CustomOAuth2User;
import org.websoso.WSSServer.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User customOAuth2UserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String socialId = customOAuth2UserDetails.getName();
        User user = userRepository.findBySocialId(socialId);
        UserAuthentication userAuthentication = new UserAuthentication(user.getUserId(), null, null);
        String accessToken = jwtProvider.generateAccessToken(userAuthentication);
        String refreshToken = jwtProvider.generateRefreshToken(userAuthentication);

        boolean isRegister = !user.getNickname().contains("*");

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter()
                .write("{\"Authorization\": \"" + accessToken + "\", \"refreshToken\": \"" + refreshToken + "\"}");
    }
}
