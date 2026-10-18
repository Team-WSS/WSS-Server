package org.websoso.WSSServer.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.exception.error.CustomAvatarError.UNSELECTABLE_AVATAR;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_AVATAR;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.dto.user.UpdateMyProfileRequest;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.repository.GenreRepository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.AvatarProfileRepository;
import org.websoso.WSSServer.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceAvatarSelectionTest {

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
    private User user;

    @DisplayName("프로필 변경 API에서 일반 계정은 -1, -2 아바타를 직접 지정할 수 없다")
    @ParameterizedTest
    @ValueSource(longs = {-1L, -2L})
    void rejectsRestrictedAvatarProfile(long avatarProfileId) {
        given(user.getAvatarProfileId()).willReturn(1L);

        assertThatThrownBy(() -> userService.updateProfileInfo(user, avatarRequest(avatarProfileId)))
                .isInstanceOfSatisfying(CustomAvatarException.class,
                        exception -> assertThat(exception.getICustomError()).isEqualTo(UNSELECTABLE_AVATAR));

        then(user).should(never()).updateUserProfile(any(), any(), any());
        then(genrePreferenceRepository).shouldHaveNoInteractions();
    }

    @DisplayName("프로필 변경 API에서 -2를 사용 중인 계정도 -1 아바타는 지정할 수 없다")
    @Test
    void rejectsAdminAvatarProfileForOfficialAccount() {
        given(user.getAvatarProfileId()).willReturn(-2L);

        assertThatThrownBy(() -> userService.updateProfileInfo(user, avatarRequest(-1L)))
                .isInstanceOfSatisfying(CustomAvatarException.class,
                        exception -> assertThat(exception.getICustomError()).isEqualTo(UNSELECTABLE_AVATAR));
    }

    @DisplayName("프로필 변경 API에서 -2를 사용 중인 계정이 -2를 다시 지정하면 기존처럼 이미 설정된 아바타로 응답한다")
    @Test
    void keepsAlreadySetResponseForCurrentOfficialAvatarProfile() {
        given(user.getAvatarProfileId()).willReturn(-2L);

        assertThatThrownBy(() -> userService.updateProfileInfo(user, avatarRequest(-2L)))
                .isInstanceOfSatisfying(CustomUserException.class,
                        exception -> assertThat(exception.getICustomError()).isEqualTo(ALREADY_SET_AVATAR));
    }

    @DisplayName("프로필 변경 API에서 -2를 사용 중인 계정은 아바타를 지정하지 않으면 현재 아바타를 유지한 채 프로필을 변경한다")
    @Test
    void keepsOfficialAvatarProfileWhenAvatarIsNotRequested() {
        given(user.getAvatarProfileId()).willReturn(-2L);
        given(user.getIntro()).willReturn("이전 소개");

        userService.updateProfileInfo(user, new UpdateMyProfileRequest(null, null, "새 소개", List.of()));

        then(user).should().updateUserProfile(null, null, "새 소개");
    }

    @DisplayName("프로필 변경 API에서 일반 아바타는 기존처럼 지정할 수 있다")
    @Test
    void updatesRegularAvatarProfile() {
        given(user.getAvatarProfileId()).willReturn(1L);

        userService.updateProfileInfo(user, avatarRequest(4L));

        then(user).should().updateUserProfile(4L, null, null);
    }

    @DisplayName("Deprecated 프로필 변경 API에서도 -1, -2 아바타를 직접 지정할 수 없다")
    @ParameterizedTest
    @ValueSource(longs = {-1L, -2L})
    void rejectsRestrictedAvatarProfileOnDeprecatedApi(long avatarId) {
        given(user.getAvatarId()).willReturn((byte) 1);

        assertThatThrownBy(() -> userService.updateMyProfileInfo(user, avatarRequest(avatarId)))
                .isInstanceOfSatisfying(CustomAvatarException.class,
                        exception -> assertThat(exception.getICustomError()).isEqualTo(UNSELECTABLE_AVATAR));

        then(user).should(never()).updateUserProfile(any(UpdateMyProfileRequest.class));
        then(genrePreferenceRepository).shouldHaveNoInteractions();
    }

    @DisplayName("Deprecated 프로필 변경 API에서 일반 아바타는 기존처럼 지정할 수 있다")
    @Test
    void updatesRegularAvatarOnDeprecatedApi() {
        given(user.getAvatarId()).willReturn((byte) 1);
        UpdateMyProfileRequest request = avatarRequest(2L);

        userService.updateMyProfileInfo(user, request);

        then(user).should().updateUserProfile(request);
    }

    private UpdateMyProfileRequest avatarRequest(Long avatarId) {
        return new UpdateMyProfileRequest(avatarId, null, null, List.of());
    }
}
