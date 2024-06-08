package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.avatar.AvatarErrorCode.AVATAR_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.exception.avatar.exception.AvatarNotFoundException;
import org.websoso.WSSServer.repository.AvatarRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarService {

    private final AvatarRepository avatarRepository;

    public Avatar getAvatarOrException(Byte avatarId) {
        return avatarRepository.findById(avatarId).orElseThrow(() ->
                new AvatarNotFoundException(AVATAR_NOT_FOUND, "avatar with the given id was not found"));
    }
}
