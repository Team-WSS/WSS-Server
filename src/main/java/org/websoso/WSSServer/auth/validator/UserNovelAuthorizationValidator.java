package org.websoso.WSSServer.auth.validator;

import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Component
@RequiredArgsConstructor
public class UserNovelAuthorizationValidator implements ResourceAuthorizationValidator {

    private final NovelRepository novelRepository;
    private final UserNovelRepository userNovelRepository;

    @Override
    public boolean hasPermission(Long resourceId, User user) {
        Novel novel = getNovelOrException(resourceId);
        UserNovel userNovel = getUserNovelOrException(user, novel);
        return true;
    }

    private Novel getNovelOrException(Long novelId) {
        return novelRepository.findById(novelId)
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"));
    }

    private UserNovel getUserNovelOrException(User user, Novel novel) {
        return userNovelRepository.findByNovelAndUser(novel, user)
                .orElseThrow(() -> new CustomUserNovelException(USER_NOVEL_NOT_FOUND,
                        "user novel with the given user and novel is not found"));
    }

    @Override
    public Class<?> getResourceType() {
        return UserNovel.class;
    }
}
