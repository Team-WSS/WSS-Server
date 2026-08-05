package org.websoso.WSSServer.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.auth.client.dto.KakaoUserInfo;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.repository.GenreRepository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.AvatarProfileRepository;
import org.websoso.WSSServer.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceSocialUserTest {

    private static final Long KAKAO_ID = 1234567890L;
    private static final String KAKAO_SOCIAL_ID = "kakao_1234567890";
    private static final String KAKAO_DEFAULT_NICKNAME = "k*34567890";
    private static final String APPLE_SOCIAL_ID = "apple_001234.abcdefghijklmn.1234";
    private static final String APPLE_DEFAULT_NICKNAME = "a*abcdefgh";
    private static final String EMAIL = "websoso@websoso.org";

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AvatarProfileRepository avatarProfileRepository;

    @Mock
    private GenrePreferenceRepository genrePreferenceRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private BlockService blockService;

    @Mock
    private DiscordMessageClient discordMessageClient;

    @Mock
    private User existingUser;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @DisplayName("카카오 소셜 식별자로 가입된 사용자가 있으면 새로 저장하지 않고 기존 사용자를 반환한다")
    @Test
    void getOrCreateKakaoUser_existingUser_returnsWithoutSaving() {
        given(userRepository.findBySocialId(KAKAO_SOCIAL_ID)).willReturn(existingUser);

        User result = userService.getOrCreateKakaoUser(kakaoUserInfo());

        assertThat(result).isSameAs(existingUser);
        then(userRepository).should(never()).save(any(User.class));
    }

    @DisplayName("카카오 소셜 식별자로 가입된 사용자가 없으면 임시 닉네임을 가진 사용자를 생성해 저장한다")
    @Test
    void getOrCreateKakaoUser_newUser_savesWithTemporaryNickname() {
        given(userRepository.findBySocialId(KAKAO_SOCIAL_ID)).willReturn(null);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        User result = userService.getOrCreateKakaoUser(kakaoUserInfo());

        then(userRepository).should().save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(result).isSameAs(saved);
        assertThat(saved.getSocialId()).isEqualTo(KAKAO_SOCIAL_ID);
        assertThat(saved.getNickname()).isEqualTo(KAKAO_DEFAULT_NICKNAME);
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.isTemporaryNickname()).isTrue();
    }

    @DisplayName("애플 소셜 식별자로 가입된 사용자가 있으면 새로 저장하지 않고 기존 사용자를 반환한다")
    @Test
    void getOrCreateAppleUser_existingUser_returnsWithoutSaving() {
        given(userRepository.findBySocialId(APPLE_SOCIAL_ID)).willReturn(existingUser);

        User result = userService.getOrCreateAppleUser(APPLE_SOCIAL_ID, EMAIL, APPLE_DEFAULT_NICKNAME);

        assertThat(result).isSameAs(existingUser);
        then(userRepository).should(never()).save(any(User.class));
    }

    @DisplayName("애플 소셜 식별자로 가입된 사용자가 없으면 전달받은 닉네임으로 사용자를 생성해 저장한다")
    @Test
    void getOrCreateAppleUser_newUser_savesWithGivenNickname() {
        given(userRepository.findBySocialId(APPLE_SOCIAL_ID)).willReturn(null);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        User result = userService.getOrCreateAppleUser(APPLE_SOCIAL_ID, EMAIL, APPLE_DEFAULT_NICKNAME);

        then(userRepository).should().save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(result).isSameAs(saved);
        assertThat(saved.getSocialId()).isEqualTo(APPLE_SOCIAL_ID);
        assertThat(saved.getNickname()).isEqualTo(APPLE_DEFAULT_NICKNAME);
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.isTemporaryNickname()).isTrue();
    }

    private KakaoUserInfo kakaoUserInfo() {
        return new KakaoUserInfo(
                KAKAO_ID,
                new KakaoUserInfo.Properties("websoso"),
                new KakaoUserInfo.KakaoAccount(EMAIL)
        );
    }
}
