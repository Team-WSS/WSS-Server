package org.websoso.WSSServer.auth.validator;

import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.novel.repository.NovelRepository;

@Component
@RequiredArgsConstructor
public class NovelAuthorizationValidator implements ResourceAuthorizationValidator {

    private final NovelRepository novelRepository;

    @Override
    public boolean hasPermission(Long resourceId, User user) {
        Novel novel = getNovelOrException(resourceId);
        return true;
    }

    private Novel getNovelOrException(Long novelId) {
        return novelRepository.findById(novelId)
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"));
    }

    @Override
    public Class<?> getResourceType() {
        return Novel.class;
    }
}
