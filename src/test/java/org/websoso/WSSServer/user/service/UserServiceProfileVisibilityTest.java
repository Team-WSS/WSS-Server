package org.websoso.WSSServer.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.dto.user.ProfileGetResponse;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.repository.GenreRepository;
import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.AvatarProfileRepository;
import org.websoso.WSSServer.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceProfileVisibilityTest {

    private static final Long VISITOR_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long AVATAR_ID = 10L;

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
    private User visitor;

    @Mock
    private User owner;

    @Mock
    private AvatarProfile avatar;

    @DisplayName("비공개 프로필도 차단 관계가 아니면 기존 프로필 정보와 함께 조회된다")
    @Test
    void returnsPrivateProfileOfOtherUser() {
        givenOwnerProfile(false);
        given(visitor.getUserId()).willReturn(VISITOR_ID);

        ProfileGetResponse response = userService.getProfileInfo(visitor, OWNER_ID);

        assertThat(response.isProfilePublic()).isFalse();
        assertThat(response.nickname()).isEqualTo("소소한 독자");
        assertThat(response.intro()).isEqualTo("한 줄 소개");
        assertThat(response.avatarImage()).isEqualTo("avatar.png");
        assertThat(response.genrePreferences()).containsExactly("로맨스");
        then(blockService).should().validateNotBlocked(VISITOR_ID, OWNER_ID);
    }

    @DisplayName("비로그인 사용자도 비공개 프로필을 조회할 수 있다")
    @Test
    void returnsPrivateProfileForAnonymousVisitor() {
        givenOwnerProfile(false);

        ProfileGetResponse response = userService.getProfileInfo(null, OWNER_ID);

        assertThat(response.isProfilePublic()).isFalse();
        assertThat(response.nickname()).isEqualTo("소소한 독자");
        then(blockService).should().validateNotBlocked(null, OWNER_ID);
    }

    @DisplayName("공개 프로필은 공개 여부를 true로 응답한다")
    @Test
    void returnsPublicProfileOfOtherUser() {
        givenOwnerProfile(true);
        given(visitor.getUserId()).willReturn(VISITOR_ID);

        ProfileGetResponse response = userService.getProfileInfo(visitor, OWNER_ID);

        assertThat(response.isProfilePublic()).isTrue();
    }

    @DisplayName("본인 프로필은 비공개여도 공개 여부를 true로 응답한다")
    @Test
    void returnsOwnPrivateProfileAsPublic() {
        givenOwnerProfileFields();

        ProfileGetResponse response = userService.getProfileInfo(owner, OWNER_ID);

        assertThat(response.isProfilePublic()).isTrue();
    }

    private void givenOwnerProfile(boolean isProfilePublic) {
        givenOwnerProfileFields();
        given(owner.getIsProfilePublic()).willReturn(isProfilePublic);
    }

    private void givenOwnerProfileFields() {
        given(userRepository.findById(OWNER_ID)).willReturn(Optional.of(owner));
        given(owner.getUserId()).willReturn(OWNER_ID);
        given(owner.getAvatarProfileId()).willReturn(AVATAR_ID);
        given(owner.getNickname()).willReturn("소소한 독자");
        given(owner.getIntro()).willReturn("한 줄 소개");
        given(avatarProfileRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar));
        given(avatar.getAvatarProfileImage()).willReturn("avatar.png");
        List<GenrePreference> genrePreferences = List.of(genrePreference("로맨스"));
        given(genrePreferenceRepository.findByUser(owner)).willReturn(genrePreferences);
    }

    private GenrePreference genrePreference(String genreName) {
        Genre genre = mock(Genre.class);
        given(genre.getGenreName()).willReturn(genreName);
        GenrePreference genrePreference = mock(GenrePreference.class);
        given(genrePreference.getGenre()).willReturn(genre);
        return genrePreference;
    }
}
