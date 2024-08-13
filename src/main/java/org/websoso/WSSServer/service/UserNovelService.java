package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.PRIVATE_PROFILE_STATUS;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.NOT_EVALUATED;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_ALREADY_EXISTS;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_NOT_FOUND;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelGenre;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.UserNovelAttractivePoint;
import org.websoso.WSSServer.domain.UserNovelKeyword;
import org.websoso.WSSServer.domain.common.Gender;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserGenrePreferenceGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserGenrePreferencesGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelAndNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelAndNovelsGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.dto.userNovel.UserNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelUpdateRequest;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.repository.GenreRepository;
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
    private final UserService userService;
    private final GenreRepository genreRepository;

    private static final List<String> priorityGenreNamesOfMale = List.of(
            "fantasy", "modernFantasy", "wuxia", "drama", "mystery", "lightNovel", "romance", "romanceFantasy", "BL"
    );
    private static final List<String> priorityGenreNamesOfFemale = List.of(
            "romance", "romanceFantasy", "fantasy", "modernFantasy", "wuxia", "drama", "mystery", "lightNovel", "BL"
    );

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

    @Transactional(readOnly = true)
    public UserNovelAndNovelsGetResponse getUserNovelsAndNovels(User visitor, Long ownerId, String readStatus,
                                                                Long lastUserNovelId, int size, String sortType) {
        User owner = userService.getUserOrException(ownerId);

        if (owner.getIsProfilePublic() || isOwner(visitor, ownerId)) {
            // TODO 성능 개선
            List<UserNovel> userNovelsByUserAndSortType =
                    userNovelRepository.findByUserAndReadStatus(owner, readStatus);
            long evaluatedUserNovelCount = userNovelsByUserAndSortType.stream()
                    .filter(userNovel -> userNovel.getUserNovelRating() != 0.0f)
                    .count();
            float evaluatedUserNovelSum = (float) userNovelsByUserAndSortType
                    .stream()
                    .filter(userNovel -> userNovel.getUserNovelRating() != 0.0f)
                    .mapToDouble(UserNovel::getUserNovelRating)
                    .sum();
            Float evaluatedUserNovelRating = evaluatedUserNovelCount > 0
                    ? evaluatedUserNovelSum / evaluatedUserNovelCount
                    : 0;
            Long userNovelCount = (long) userNovelsByUserAndSortType.size();

            List<UserNovel> userNovelsByNoOffsetPagination = userNovelRepository.findUserNovelsByNoOffsetPagination(
                    owner, lastUserNovelId, size, readStatus, sortType);
            // TODO Slice의 hasNext()로 판단하도록 수정
            Boolean isLoadable = userNovelsByNoOffsetPagination.size() == size;
            List<UserNovelAndNovelGetResponse> userNovelAndNovelGetResponses = userNovelsByNoOffsetPagination.stream()
                    .map(UserNovelAndNovelGetResponse::of)
                    .toList();

            return UserNovelAndNovelsGetResponse.of(userNovelCount, evaluatedUserNovelRating, isLoadable,
                    userNovelAndNovelGetResponses);
        }

        throw new CustomUserException(PRIVATE_PROFILE_STATUS, "the profile status of the user is set to private");
    }

    @Transactional(readOnly = true)
    public UserGenrePreferencesGetResponse getUserGenrePreferences(User visitor, Long ownerId) {
        User owner = userService.getUserOrException(ownerId);

        if (owner.getIsProfilePublic() || isOwner(visitor, ownerId)) {
            //TODO genreMap은 Genre의 변화가 없다면 매번 repository에서 가져올 필요가 없음 -> 캐싱하여 사용하도록 리팩터링
            List<Genre> allGenres = genreRepository.findAll();

            Map<Genre, Long> myGenreCountMap = userNovelRepository.findUserNovelByUser(owner)
                    .stream()
                    .map(UserNovel::getNovel)
                    .map(Novel::getNovelGenres)
                    .flatMap(List::stream)
                    .map(NovelGenre::getGenre)
                    .collect(Collectors.groupingBy(genre -> genre, Collectors.counting()));

            allGenres.forEach(genre -> myGenreCountMap.putIfAbsent(genre, 0L));

            List<Genre> priorityOrderByGender = getPriorityOrderByGender(owner.getGender(), allGenres);

            List<UserGenrePreferenceGetResponse> genrePreferences = myGenreCountMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.<Genre, Long>comparingByValue().reversed()
                            .thenComparing(entry -> priorityOrderByGender.indexOf(entry.getKey())))
                    .map(preferGenre -> UserGenrePreferenceGetResponse.of(preferGenre.getKey(), preferGenre.getValue()))
                    .toList();

            return UserGenrePreferencesGetResponse.of(genrePreferences);
        }

        throw new CustomUserException(PRIVATE_PROFILE_STATUS, "the profile status of the user is set to private");
    }

    private static boolean isOwner(User visitor, Long ownerId) {
        //TODO 현재는 비로그인 회원인 경우
        return visitor != null && visitor.getUserId().equals(ownerId);
    }

    private List<Genre> getPriorityOrderByGender(Gender gender, List<Genre> allGenres) {
        List<String> priorityGenreNames = gender.equals(Gender.M)
                ? priorityGenreNamesOfMale
                : priorityGenreNamesOfFemale;

        return priorityGenreNames.stream()
                .map(name -> allGenres.stream()
                        .filter(genre -> genre.getGenreName().equals(name))
                        .findFirst()
                        .orElseThrow(() -> new CustomGenreException(GENRE_NOT_FOUND,
                                "genre with the given genreName is not found"))
                )
                .toList();
    }
}
