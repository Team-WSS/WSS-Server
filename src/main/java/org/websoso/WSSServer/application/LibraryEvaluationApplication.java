package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_ALREADY_EXISTS;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.service.AttractivePointService;
import org.websoso.WSSServer.library.service.KeywordService;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;

@Service
@RequiredArgsConstructor
public class LibraryEvaluationApplication {

    private final NovelServiceImpl novelService;
    private final LibraryService libraryService;
    private final AttractivePointService attractivePointService;
    private final KeywordService keywordService;

    @Transactional
    public void createEvaluation(User user, UserNovelCreateRequest request) {
        Novel novel = novelService.getNovelOrException(request.novelId());

        try {
            UserNovel userNovel = libraryService.createLibrary(request.status(), request.userNovelRating(),
                    request.startDate(), request.endDate(), user, novel);

            attractivePointService.createUserNovelAttractivePoints(userNovel, request.attractivePoints());
            keywordService.createNovelKeywords(userNovel, request.keywordIds());
        } catch (DataIntegrityViolationException e) {
            throw new CustomUserNovelException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
        }
    }
}
