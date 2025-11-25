package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.exception.error.CustomUserNovelError.NOT_EVALUATED;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_ALREADY_EXISTS;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.dto.userNovel.UserNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelUpdateRequest;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.library.domain.AttractivePoint;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelAttractivePoint;
import org.websoso.WSSServer.library.domain.UserNovelKeyword;
import org.websoso.WSSServer.library.service.AttractivePointService;
import org.websoso.WSSServer.library.service.KeywordService;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;

/**
 * 서재 평가는 서재와 매력 포인트, 키워드가 핵심 도메인이다.
 */
@Service
@RequiredArgsConstructor
public class LibraryEvaluationApplication {

    private final NovelServiceImpl novelService;
    private final LibraryService libraryService;
    private final AttractivePointService attractivePointService;
    private final KeywordService keywordService;

    /**
     * 서재 평가를 생성한다.
     *
     * @param user    사용자 객체
     * @param request UserNovelCreateRequest
     */
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

    /**
     * 서재 평가를 불러온다.
     *
     * @param user    사용자 객체
     * @param novelId 소설 ID
     * @return UserNovelGetResponse
     */
    public UserNovelGetResponse getEvaluation(User user, Long novelId) {
        Novel novel = novelService.getNovelOrException(novelId);
        UserNovel userNovel = libraryService.getUserNovelOrNull(user, novel);

        if (userNovel == null) {
            return UserNovelGetResponse.of(novel, null, Collections.emptyList(), Collections.emptyList());
        }

        List<String> attractivePoints = getStringAttractivePoints(userNovel);
        List<KeywordGetResponse> keywords = getKeywordGetResponses(userNovel);

        return UserNovelGetResponse.of(novel, userNovel, attractivePoints, keywords);
    }

    /**
     * 서재 평가를 업데이트한다.
     *
     * @param user    사용자 객체
     * @param novelId 소설 ID
     * @param request UserNovelUpdateRequest
     */
    @Transactional
    public void updateEvaluation(User user, Long novelId, UserNovelUpdateRequest request) {
        UserNovel userNovel = libraryService.getUserNovelOrException(user, novelId);

        userNovel.updateUserNovel(request.userNovelRating(), request.status(), request.startDate(), request.endDate());

        updateAttractivePoints(userNovel, request.attractivePoints());

        updateKeywords(userNovel, request.keywordIds());
    }

    /**
     * 서재 평가를 삭제한다.
     *
     * @param user    사용자 객체
     * @param novelId 소설 ID
     */
    public void deleteEvaluation(User user, Long novelId) {
        UserNovel userNovel = libraryService.getUserNovelOrException(user, novelId);

        if (userNovel.getStatus() == null) {
            throw new CustomUserNovelException(NOT_EVALUATED, "this novel has not been evaluated by the user");
        }

        if (userNovel.getIsInterest()) {
            userNovel.deleteEvaluation();

            attractivePointService.deleteUserNovelAttractivePoints(userNovel.getUserNovelAttractivePoints());
            keywordService.deleteUserNovelKeywords(userNovel.getUserNovelKeywords());
        } else {
            libraryService.delete(userNovel);
        }
    }

    // TODO: 리팩토링 대상 Fetch Lazy 수정
    private List<String> getStringAttractivePoints(UserNovel userNovel) {
        return userNovel.getUserNovelAttractivePoints().stream()
                .map(attractivePoint -> attractivePoint.getAttractivePoint().getAttractivePointName())
                .toList();
    }

    // TODO: 리팩토링 대상 Fetch Lazy 수정
    private List<KeywordGetResponse> getKeywordGetResponses(UserNovel userNovel) {
        return userNovel.getUserNovelKeywords().stream()
                .map(userNovelKeyword -> KeywordGetResponse.of(userNovelKeyword.getKeyword()))
                .toList();
    }

    private void updateAttractivePoints(UserNovel userNovel, List<String> attractivePoints) {
        Map<AttractivePoint, UserNovelAttractivePoint> currentPointMap = userNovel.getUserNovelAttractivePoints()
                .stream()
                .collect(Collectors.toMap(UserNovelAttractivePoint::getAttractivePoint, it -> it));

        Set<AttractivePoint> requestedPoints = attractivePoints.stream()
                .map(attractivePointService::getAttractivePointByString)
                .collect(Collectors.toSet());

        addUserNovelAttractivePoints(userNovel, currentPointMap, requestedPoints);
        deleteUserNovelAttractivePoints(userNovel, currentPointMap, requestedPoints);
    }

    private void updateKeywords(UserNovel userNovel, List<Integer> keywordIds) {
        Map<Keyword, UserNovelKeyword> currentKeywordMap = userNovel.getUserNovelKeywords()
                .stream()
                .collect(Collectors.toMap(UserNovelKeyword::getKeyword, it -> it));

        Set<Keyword> requestedKeywords = keywordIds.stream()
                .map(keywordService::getKeywordOrException)
                .collect(Collectors.toSet());

        addUserNovelKeywords(userNovel, currentKeywordMap, requestedKeywords);
        deleteUserNovelKeywords(userNovel, currentKeywordMap, requestedKeywords);
    }

    private void addUserNovelKeywords(UserNovel userNovel,
                                      Map<Keyword, UserNovelKeyword> currentKeywordMap,
                                      Set<Keyword> requestedKeywords) {
        for (Keyword requested : requestedKeywords) {
            if (!currentKeywordMap.containsKey(requested)) {
                keywordService.createNovelKeyword(userNovel, requested);
            }
        }
    }

    private void deleteUserNovelKeywords(UserNovel userNovel,
                                         Map<Keyword, UserNovelKeyword> currentKeywordMap,
                                         Set<Keyword> requestedKeywords) {
        List<UserNovelKeyword> toDelete = new ArrayList<>();
        for (Map.Entry<Keyword, UserNovelKeyword> entry : currentKeywordMap.entrySet()) {
            if (!requestedKeywords.contains(entry.getKey())) {
                toDelete.add(entry.getValue());
            }
        }
        if (!toDelete.isEmpty()) {
            userNovel.getUserNovelKeywords().removeAll(toDelete);
            userNovel.touch();
        }
    }

    private void addUserNovelAttractivePoints(UserNovel userNovel,
                                              Map<AttractivePoint, UserNovelAttractivePoint> currentPointMap,
                                              Set<AttractivePoint> requestedPoints) {
        for (AttractivePoint requested : requestedPoints) {
            if (!currentPointMap.containsKey(requested)) {
                attractivePointService.createUserNovelAttractivePoint(userNovel, requested);
            }
        }
    }

    private void deleteUserNovelAttractivePoints(UserNovel userNovel,
                                                 Map<AttractivePoint, UserNovelAttractivePoint> currentPointMap,
                                                 Set<AttractivePoint> requestedPoints) {
        List<UserNovelAttractivePoint> toDelete = new ArrayList<>();
        for (Map.Entry<AttractivePoint, UserNovelAttractivePoint> entry : currentPointMap.entrySet()) {
            if (!requestedPoints.contains(entry.getKey())) {
                toDelete.add(entry.getValue());
            }
        }
        if (!toDelete.isEmpty()) {
            userNovel.getUserNovelAttractivePoints().removeAll(toDelete);
            userNovel.touch();
        }
    }

}
