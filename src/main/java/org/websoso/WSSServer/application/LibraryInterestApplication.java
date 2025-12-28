package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.exception.error.CustomUserNovelError.NOT_INTERESTED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;

@Service
@RequiredArgsConstructor
public class LibraryInterestApplication {

    private final NovelServiceImpl novelService;
    private final LibraryService libraryService;

    /**
     * 관심있어요를 남긴다.
     *
     * @param user    사용자 객체
     * @param novelId 소설 ID
     */
    @Transactional
    public void registerAsInterest(User user, Long novelId) {
        Novel novel = novelService.getNovelOrException(novelId);

        libraryService.registerInterest(user, novel);
    }

    /**
     * 관심있어요를 삭제한다.
     *
     * @param user    사용자 객체
     * @param novelId 소설 ID
     */
    @Transactional
    public void unregisterAsInterest(User user, Long novelId) {
        UserNovel userNovel = libraryService.getLibraryOrException(user, novelId);

        if (!userNovel.getIsInterest()) {
            throw new CustomUserNovelException(NOT_INTERESTED, "not registered as interest");
        }

        userNovel.setIsInterest(false);

        if (userNovel.getStatus() == null) {
            libraryService.delete(userNovel);
        }

    }

}
