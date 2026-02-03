package org.websoso.WSSServer.user.service;

import static org.websoso.WSSServer.exception.error.CustomAvatarError.AVATAR_NOT_FOUND;

import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.user.domain.Avatar;
import org.websoso.WSSServer.user.domain.AvatarLine;
import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.user.domain.AvatarProfileLine;
import org.websoso.WSSServer.dto.avatar.AvatarProfileGetResponse;
import org.websoso.WSSServer.dto.avatar.AvatarProfilesGetResponse;
import org.websoso.WSSServer.user.repository.AvatarProfileRepository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.dto.avatar.AvatarGetResponse;
import org.websoso.WSSServer.dto.avatar.AvatarsGetResponse;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.user.repository.AvatarRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AvatarService {

    private final AvatarRepository avatarRepository;
    private final AvatarProfileRepository avatarProfileRepository;

    private static final long ADMIN_AVATAR_PROFILE_ID = -1L;

    private static final Random random = new Random();      //TODO thread-safe하지 않아서 multi-thread 환경에서는 사용X

    @Transactional(readOnly = true)
    public AvatarProfile getAvatarProfileOrException(Long avatarProfileId) {
        return avatarProfileRepository.findById(avatarProfileId).orElseThrow(() ->
                new CustomAvatarException(AVATAR_NOT_FOUND, "avatar with the given id was not found"));
    }

    public AvatarProfilesGetResponse getAvatarProfileList(User user) {
        long representativeAvatarProfileId = user.getAvatarProfileId();

        List<AvatarProfile> avatarProfiles = avatarProfileRepository.findAllByAvatarProfileIdNot(
                ADMIN_AVATAR_PROFILE_ID);

        List<AvatarProfileGetResponse> avatarProfileGetResponses = avatarProfiles.stream()
                .map(avatarProfile -> {
                    List<AvatarProfileLine> avatarProfileLines = avatarProfile.getAvatarLines();
                    return AvatarProfileGetResponse.of(avatarProfile, getRandomAvatarProfileLine(avatarProfileLines),
                            representativeAvatarProfileId);
                }).toList();

        return new AvatarProfilesGetResponse(avatarProfileGetResponses);

    }

    public List<AvatarProfile> findAllByIds(List<Long> avatarProfileIds) {
        return avatarProfileRepository.findAllById(avatarProfileIds);
    }

    private static AvatarProfileLine getRandomAvatarProfileLine(List<AvatarProfileLine> avatarLines) {
        final int avatarLineSize = avatarLines.size();
        return avatarLines.get(random.nextInt(avatarLineSize));
    }

}
