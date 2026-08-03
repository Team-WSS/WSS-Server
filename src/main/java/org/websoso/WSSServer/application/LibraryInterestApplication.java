package org.websoso.WSSServer.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelStatisticsContribution;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.novel.service.NovelStatisticsService;

@Service
@RequiredArgsConstructor
public class LibraryInterestApplication {

    private final NovelServiceImpl novelService;
    private final LibraryService libraryService;
    private final NovelStatisticsService novelStatisticsService;

    /**
     * 관심있어요를 남긴다.
     *
     * @param user    사용자 객체
     * @param novelId 소설 ID
     */
    @Transactional
    public void registerAsInterest(User user, Long novelId) {
        Novel novel = novelService.getNovelOrException(novelId);
        UserNovel library = libraryService.getOrCreateLibraryForInterest(user, novel);
        NovelStatisticsContribution before = NovelStatisticsContribution.from(library);

        libraryService.registerInterest(library);
        novelStatisticsService.updateByDelta(
                novelId,
                before,
                NovelStatisticsContribution.from(library)
        );
    }

    /**
     * 관심있어요를 삭제한다.
     *
     * @param user    사용자 객체
     * @param novelId 소설 ID
     */
    @Transactional
    public void unregisterAsInterest(User user, Long novelId) {
        novelService.getNovelOrException(novelId);
        UserNovel library = libraryService.getLibraryForUpdateOrException(user, novelId);

        if (Boolean.FALSE.equals(library.getIsInterest())) {
            return;
        }

        NovelStatisticsContribution before = NovelStatisticsContribution.from(library);
        libraryService.unregisterInterest(library);
        novelStatisticsService.updateByDelta(
                novelId,
                before,
                NovelStatisticsContribution.from(library)
        );
    }

}
