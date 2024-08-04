package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.NOT_EVALUATED;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_ALREADY_EXISTS;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_NOT_FOUND;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.UserNovelAttractivePoint;
import org.websoso.WSSServer.domain.UserNovelKeyword;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.dto.userNovel.UserNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelUpdateRequest;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.UserNovelAttractivePointRepository;
import org.websoso.WSSServer.repository.UserNovelKeywordRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserNovelService {

    private final NovelRepository novelRepository;
    private final UserNovelRepository userNovelRepository;
    private final KeywordService keywordService;
    private final UserNovelAttractivePointRepository userNovelAttractivePointRepository;
    private final UserNovelKeywordRepository userNovelKeywordRepository;
    private final AttractivePointService attractivePointService;

    @Transactional(readOnly = true)
    public UserNovel getUserNovelOrException(User user, Novel novel) {
        return userNovelRepository.findByNovelAndUser(novel, user).orElseThrow(
                () -> new CustomUserNovelException(USER_NOVEL_NOT_FOUND,
                        "user novel with the given user and novel is not found"));
    }

    @Transactional(readOnly = true)
    public UserNovel getUserNovelOrNull(User user, Novel novel) {
        if (user == null) {
            return null;
        }
        return userNovelRepository.findByNovelAndUser(novel, user).orElse(null);
    }

    public void createEvaluation(User user, UserNovelCreateRequest request) {

        Novel novel = novelRepository.findById(request.novelId())
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"));

        if (getUserNovelOrNull(user, novel) != null) {
            throw new CustomUserNovelException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
        }

        UserNovel userNovel = userNovelRepository.save(UserNovel.create(
                request.status(),
                request.userNovelRating(),
                request.startDate(),
                request.endDate(),
                user,
                novel));

        createUserNovelAttractivePoints(userNovel, request.attractivePoints());
        createNovelKeywords(userNovel, request.keywordIds());

    }

    public void updateEvaluation(User user, Novel novel, UserNovelUpdateRequest request) {

        UserNovel userNovel = getUserNovelOrException(user, novel);

        if (userNovel.getStatus() == null) {
            throw new CustomUserNovelException(NOT_EVALUATED, "this novel has not been evaluated by the user");
        }

        updateUserNovel(userNovel, request);
        updateAssociations(userNovel, request);

    }

    private void updateUserNovel(UserNovel userNovel, UserNovelUpdateRequest request) {
        userNovel.updateUserNovel(request.userNovelRating(), request.status(), request.startDate(), request.endDate());
    }

    private void updateAssociations(UserNovel userNovel, UserNovelUpdateRequest request) {

        Set<AttractivePoint> previousAttractivePoints = getPreviousAttractivePoints(userNovel);
        Set<Keyword> previousKeywords = getPreviousKeywords(userNovel);

        manageAttractivePoints(userNovel, request.attractivePoints(), previousAttractivePoints);
        manageKeywords(userNovel, request.keywordIds(), previousKeywords);

        userNovelAttractivePointRepository.deleteByAttractivePointsAndUserNovel(previousAttractivePoints, userNovel);
        userNovelKeywordRepository.deleteByKeywordsAndUserNovel(previousKeywords, userNovel);

    }

    private Set<AttractivePoint> getPreviousAttractivePoints(UserNovel userNovel) {
        return userNovel.getUserNovelAttractivePoints()
                .stream()
                .map(UserNovelAttractivePoint::getAttractivePoint)
                .collect(Collectors.toSet());
    }

    private Set<Keyword> getPreviousKeywords(UserNovel userNovel) {
        return userNovel.getUserNovelKeywords()
                .stream()
                .map(UserNovelKeyword::getKeyword)
                .collect(Collectors.toSet());
    }

    private void manageAttractivePoints(UserNovel userNovel, List<String> attractivePoints,
                                        Set<AttractivePoint> previousAttractivePoints) {
        for (String stringAttractivePoint : attractivePoints) {
            AttractivePoint attractivePoint = attractivePointService.getAttractivePointByString(stringAttractivePoint);
            if (previousAttractivePoints.contains(attractivePoint)) {
                previousAttractivePoints.remove(attractivePoint);
            } else {
                userNovelAttractivePointRepository.save(UserNovelAttractivePoint.create(userNovel, attractivePoint));
            }
        }
    }

    private void manageKeywords(UserNovel userNovel, List<Integer> keywordIds, Set<Keyword> previousKeywords) {
        for (Integer keywordId : keywordIds) {
            Keyword keyword = keywordService.getKeywordOrException(keywordId);
            if (previousKeywords.contains(keyword)) {
                previousKeywords.remove(keyword);
            } else {
                userNovelKeywordRepository.save(UserNovelKeyword.create(userNovel, keyword));
            }
        }
    }

    private void createUserNovelAttractivePoints(UserNovel userNovel, List<String> request) {
        for (String stringAttractivePoint : request) {
            AttractivePoint attractivePoint = attractivePointService.getAttractivePointByString(stringAttractivePoint);
            userNovelAttractivePointRepository.save(UserNovelAttractivePoint.create(userNovel, attractivePoint));
        }
    }

    private void createNovelKeywords(UserNovel userNovel, List<Integer> request) {
        for (Integer keywordId : request) {
            Keyword keyword = keywordService.getKeywordOrException(keywordId);
            userNovelKeywordRepository.save(UserNovelKeyword.create(userNovel, keyword));
        }

    }

    public void deleteUserNovel(User user, Novel novel) {

        UserNovel userNovel = getUserNovelOrException(user, novel);

        if (userNovel.getStatus() == null) {
            throw new CustomUserNovelException(NOT_EVALUATED, "this novel has not been evaluated by the user");
        }

        if (userNovel.getIsInterest()) {
            userNovel.deleteEvaluation();
            userNovelAttractivePointRepository.deleteAll(userNovel.getUserNovelAttractivePoints());
            userNovelKeywordRepository.deleteAll(userNovel.getUserNovelKeywords());
        } else {
            userNovelRepository.delete(userNovel);
        }
    }

    public UserNovel createUserNovelByInterest(User user, Novel novel) {

        if (getUserNovelOrNull(user, novel) != null) {
            throw new CustomUserNovelException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
        }

        return userNovelRepository.save(UserNovel.create(null, 0.0f, null, null, user, novel));
    }

    @Transactional(readOnly = true)
    public UserNovelGetResponse getEvaluation(User user, Novel novel) {

        UserNovel userNovel = getUserNovelOrNull(user, novel);

        if (userNovel == null) {
            return UserNovelGetResponse.of(novel, null, Collections.emptyList(), Collections.emptyList());
        }

        List<String> attractivePoints = getStringAttractivePoints(userNovel);
        List<KeywordGetResponse> keywords = getKeywordGetResponses(userNovel);

        return UserNovelGetResponse.of(novel, userNovel, attractivePoints, keywords);
    }

    private List<String> getStringAttractivePoints(UserNovel userNovel) {
        return userNovel.getUserNovelAttractivePoints().stream()
                .map(attractivePoint -> attractivePoint.getAttractivePoint().getAttractivePointName())
                .collect(Collectors.toList());
    }

    private List<KeywordGetResponse> getKeywordGetResponses(UserNovel userNovel) {
        return userNovel.getUserNovelKeywords().stream()
                .map(userNovelKeyword -> KeywordGetResponse.of(userNovelKeyword.getKeyword()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserNovelCountGetResponse getUserNovelStatistics(User user) {
        return userNovelRepository.findUserNovelStatistics(user);
    }
}
