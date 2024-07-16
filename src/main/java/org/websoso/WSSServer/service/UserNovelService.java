package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_ALREADY_EXISTS;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_NOT_FOUND;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelKeyword;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.UserNovelAttractivePoint;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.dto.userNovel.UserNovelGetResponse;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.repository.NovelKeywordRepository;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.UserNovelAttractivePointRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserNovelService {

    private final NovelRepository novelRepository;
    private final UserNovelRepository userNovelRepository;
    private final KeywordService keywordService;
    private final UserNovelAttractivePointRepository userNovelAttractivePointRepository;
    private final NovelKeywordRepository novelKeywordRepository;
    private final AttractivePointService attractivePointService;

    @Transactional(readOnly = true)
    public UserNovel getUserNovelOrNull(User user, Novel novel) {
        if (user == null) {
            return null;
        }
        return userNovelRepository.findByNovelAndUser(novel, user).orElse(null);
    }

    public void createUserNovel(User user, UserNovelCreateRequest request) {

        Novel novel = novelRepository.findById(request.novelId())
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"));

        if (getUserNovelOrNull(user, novel) != null) {
            throw new CustomUserNovelException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
        }

        UserNovel userNovel = userNovelRepository.save(UserNovel.create(
                request.status(),
                request.userNovelRating(),
                request.startDate() != null ? convertToLocalDate(request.startDate()) : null,
                request.endDate() != null ? convertToLocalDate(request.endDate()) : null,
                user,
                novel));

        for (String stringAttractivePoint : request.attractivePoints()) {
            AttractivePoint attractivePoint = attractivePointService.getAttractivePointByString(stringAttractivePoint);
            userNovelAttractivePointRepository.save(UserNovelAttractivePoint.create(userNovel, attractivePoint));
        }

        for (Integer keywordId : request.keywordIds()) {
            Keyword keyword = keywordService.getKeywordOrException(keywordId);
            novelKeywordRepository.save(NovelKeyword.create(novel, keyword, user.getUserId()));
        }

    }

    public void deleteUserNovel(User user, Novel novel) {

        UserNovel userNovel = getUserNovelOrNull(user, novel);

        if (userNovel == null) {
            throw new CustomUserNovelException(USER_NOVEL_NOT_FOUND,
                    "user novel with the given user and novel is not found");
        }

        List<UserNovelAttractivePoint> userNovelAttractivePoints = userNovelAttractivePointRepository.findAllByUserNovel(
                userNovel);
        userNovelAttractivePointRepository.deleteAll(userNovelAttractivePoints);

        List<NovelKeyword> novelKeywords = novelKeywordRepository.findAllByNovelAndUserId(novel, user.getUserId());
        novelKeywordRepository.deleteAll(novelKeywords);

        if (!userNovel.getIsInterest()) {
            userNovelRepository.delete(userNovel);
        }

        userNovel.deleteEvaluation();
    }

    public UserNovel createUserNovelByInterest(User user, Novel novel) {

        if (getUserNovelOrNull(user, novel) != null) {
            throw new CustomUserNovelException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
        }

        return userNovelRepository.save(UserNovel.create(null, 0.0f, null, null, user, novel));
    }

    private LocalDate convertToLocalDate(String string) {
        return LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
    }

    @Transactional(readOnly = true)
    public UserNovelGetResponse getUserNovelInfo(User user, Novel novel) {

        UserNovel userNovel = getUserNovelOrNull(user, novel);
        if (userNovel == null) {
            throw new CustomUserNovelException(USER_NOVEL_NOT_FOUND,
                    "user novel with the given user and novel is not found");
        }

        List<String> attractivePoints = extractAttractivePoints(userNovel);

        List<NovelKeyword> novelKeywords = novelKeywordRepository.findAllByNovelAndUserId(novel, user.getUserId());
        List<KeywordGetResponse> keywords = new ArrayList<>();
        for (NovelKeyword novelKeyword : novelKeywords) {
            keywords.add(KeywordGetResponse.of(novelKeyword.getKeyword()));
        }

        return UserNovelGetResponse.of(userNovel, attractivePoints, keywords);
    }

    private List<String> extractAttractivePoints(UserNovel userNovel) {

        List<UserNovelAttractivePoint> userNovelAttractivePoints = userNovelAttractivePointRepository.findAllByUserNovel(
                userNovel);
        List<String> attractivePoints = new ArrayList<>();

        for (UserNovelAttractivePoint attractivePoint : userNovelAttractivePoints) {
            attractivePoints.add(attractivePoint.getAttractivePoint().getAttractivePointName());
        }

        return attractivePoints;
    }

}
