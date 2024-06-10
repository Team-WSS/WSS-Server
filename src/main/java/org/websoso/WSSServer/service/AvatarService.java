package org.websoso.WSSServer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.AvatarLine;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.avatar.AvatarGetResponse;
import org.websoso.WSSServer.dto.avatar.AvatarsGetResponse;
import org.websoso.WSSServer.repository.AvatarRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AvatarService {

    private final AvatarRepository avatarRepository;

    @Transactional(readOnly = true)
    public AvatarsGetResponse getAvatarList(User user) {
        List<Avatar> avatars = avatarRepository.findAll();
        List<AvatarGetResponse> avatarGetResponses = avatars.stream()
                .map(avatar -> {
                    List<AvatarLine> avatarLines = avatar.getAvatarLine();
                    return AvatarGetResponse.of(avatar, avatarLines, user);
                }).toList();
        return new AvatarsGetResponse(avatarGetResponses);
    }
}
