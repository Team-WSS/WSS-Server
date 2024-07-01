package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomAvatarError.AVATAR_NOT_FOUND;

import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.AvatarLine;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.avatar.AvatarGetResponse;
import org.websoso.WSSServer.dto.avatar.AvatarsGetResponse;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.repository.AvatarRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AvatarService {

    private final AvatarRepository avatarRepository;
    private static final Random random = new Random();      //TODO thread-safe하지 않아서 multi-thread 환경에서는 사용X

    @Transactional(readOnly = true)
    public Avatar getAvatarOrException(Byte avatarId) {
        return avatarRepository.findById(avatarId).orElseThrow(() ->
                new CustomAvatarException(AVATAR_NOT_FOUND, "avatar with the given id was not found"));
    }

    @Transactional(readOnly = true)
    public AvatarsGetResponse getAvatarList(User user) {
        Byte representativeAvatarId = user.getAvatarId();
        List<Avatar> avatars = avatarRepository.findAll();
        List<AvatarGetResponse> avatarGetResponses = avatars.stream()
                .map(avatar -> {
                    List<AvatarLine> avatarLines = avatar.getAvatarLine();
                    return AvatarGetResponse.of(avatar, getRandomAvatarLine(avatarLines), representativeAvatarId);
                }).toList();
        return new AvatarsGetResponse(avatarGetResponses);
    }

    private static AvatarLine getRandomAvatarLine(List<AvatarLine> avatarLines) {
        final int avatarLineSize = avatarLines.size();
        return avatarLines.get(random.nextInt(avatarLineSize));
    }
}
