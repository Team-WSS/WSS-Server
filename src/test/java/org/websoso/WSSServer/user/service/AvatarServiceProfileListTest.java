package org.websoso.WSSServer.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.dto.avatar.AvatarProfileGetResponse;
import org.websoso.WSSServer.dto.avatar.AvatarProfilesGetResponse;
import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.user.domain.AvatarProfileLine;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.AvatarProfileRepository;
import org.websoso.WSSServer.user.repository.AvatarRepository;

@ExtendWith(MockitoExtension.class)
class AvatarServiceProfileListTest {

    @InjectMocks
    private AvatarService avatarService;

    @Mock
    private AvatarRepository avatarRepository;

    @Mock
    private AvatarProfileRepository avatarProfileRepository;

    @Mock
    private User user;

    @DisplayName("일반 계정의 아바타 목록에서는 관리자용 -1과 공식 계정 전용 -2를 제외한다")
    @Test
    void hidesRestrictedAvatarProfilesForRegularUser() {
        given(user.getAvatarProfileId()).willReturn(4L);
        List<AvatarProfile> visibleAvatarProfiles = List.of(avatarProfile(1L), avatarProfile(4L), avatarProfile(5L));
        given(avatarProfileRepository.findAllByAvatarProfileIdNotIn(List.of(-1L, -2L)))
                .willReturn(visibleAvatarProfiles);

        AvatarProfilesGetResponse response = avatarService.getAvatarProfileList(user);

        assertThat(response.avatarProfiles())
                .extracting(AvatarProfileGetResponse::avatarProfileId)
                .containsExactly(1L, 4L, 5L);
        assertThat(response.avatarProfiles())
                .filteredOn(AvatarProfileGetResponse::isRepresentative, true)
                .extracting(AvatarProfileGetResponse::avatarProfileId)
                .containsExactly(4L);
    }

    @DisplayName("현재 -2를 사용 중인 계정의 아바타 목록에는 -2가 대표 아바타로 포함되고 -1은 제외된다")
    @Test
    void includesOfficialAvatarProfileAsRepresentativeForCurrentOwner() {
        given(user.getAvatarProfileId()).willReturn(-2L);
        List<AvatarProfile> visibleAvatarProfiles = List.of(avatarProfile(-2L), avatarProfile(1L), avatarProfile(4L));
        given(avatarProfileRepository.findAllByAvatarProfileIdNotIn(List.of(-1L)))
                .willReturn(visibleAvatarProfiles);

        AvatarProfilesGetResponse response = avatarService.getAvatarProfileList(user);

        assertThat(response.avatarProfiles())
                .extracting(AvatarProfileGetResponse::avatarProfileId)
                .containsExactly(-2L, 1L, 4L);
        assertThat(response.avatarProfiles())
                .filteredOn(AvatarProfileGetResponse::isRepresentative, true)
                .extracting(AvatarProfileGetResponse::avatarProfileId, AvatarProfileGetResponse::avatarProfileImage)
                .containsExactly(tuple(-2L, "avatar-profile--2.png"));
    }

    private AvatarProfile avatarProfile(Long avatarProfileId) {
        AvatarProfile avatarProfile = BeanUtils.instantiateClass(AvatarProfile.class);
        ReflectionTestUtils.setField(avatarProfile, "avatarProfileId", avatarProfileId);
        ReflectionTestUtils.setField(avatarProfile, "avatarProfileName", "아바타" + avatarProfileId);
        ReflectionTestUtils.setField(avatarProfile, "avatarProfileImage", "avatar-profile-" + avatarProfileId + ".png");
        ReflectionTestUtils.setField(avatarProfile, "avatarCharacterImage", "avatar-character-" + avatarProfileId + ".png");
        AvatarProfileLine line = mock(AvatarProfileLine.class);
        given(line.getAvatarLine()).willReturn("대사" + avatarProfileId);
        ReflectionTestUtils.setField(avatarProfile, "avatarLines", List.of(line));
        return avatarProfile;
    }
}
