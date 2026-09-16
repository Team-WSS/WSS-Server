package org.websoso.WSSServer.user.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.user.exception.CustomBlockError.BLOCKED_USER_ACCESS;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.repository.GenreRepository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.CustomBlockException;
import org.websoso.WSSServer.user.repository.AvatarProfileRepository;
import org.websoso.WSSServer.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceBlockPolicyTest {

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

    @DisplayName("차단 관계인 사용자의 프로필에 접근할 수 없다")
    @Test
    void rejectsBlockedUserProfile() {
        given(visitor.getUserId()).willReturn(1L);
        given(owner.getUserId()).willReturn(2L);
        given(userRepository.findById(2L)).willReturn(Optional.of(owner));
        willThrow(new CustomBlockException(BLOCKED_USER_ACCESS, "blocked"))
                .given(blockService).validateNotBlocked(1L, 2L);

        assertThatThrownBy(() -> userService.getProfileInfo(visitor, 2L))
                .isInstanceOf(CustomBlockException.class);

        then(avatarProfileRepository).shouldHaveNoInteractions();
        then(genrePreferenceRepository).shouldHaveNoInteractions();
    }

    @DisplayName("비공개 프로필도 차단 관계면 프로필 공개 여부와 무관하게 차단 오류가 발생한다")
    @Test
    void rejectsBlockedUserPrivateProfile() {
        given(visitor.getUserId()).willReturn(1L);
        given(owner.getUserId()).willReturn(2L);
        given(userRepository.findById(2L)).willReturn(Optional.of(owner));
        willThrow(new CustomBlockException(BLOCKED_USER_ACCESS, "blocked"))
                .given(blockService).validateNotBlocked(1L, 2L);

        assertThatThrownBy(() -> userService.getProfileInfo(visitor, 2L))
                .isInstanceOf(CustomBlockException.class)
                .extracting(exception -> ((CustomBlockException) exception).getICustomError())
                .isEqualTo(BLOCKED_USER_ACCESS);

        then(owner).should(never()).getIsProfilePublic();
    }
}
