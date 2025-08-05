package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Gender.M;
import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.PRIVATE_PROFILE_STATUS;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.NOT_EVALUATED;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_ALREADY_EXISTS;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelGenre;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.UserNovelAttractivePoint;
import org.websoso.WSSServer.domain.UserNovelKeyword;
import org.websoso.WSSServer.domain.common.Gender;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.dto.keyword.KeywordGetResponse;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;
import org.websoso.WSSServer.dto.userNovel.TasteKeywordGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserGenrePreferenceGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserGenrePreferencesGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelAndNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelAndNovelsGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.dto.userNovel.UserNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelUpdateRequest;
import org.websoso.WSSServer.dto.userNovel.UserTasteAttractivePointPreferencesAndKeywordsGetResponse;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.repository.FeedRepository;
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
    private final FeedRepository feedRepository;

    public static final String SORT_TYPE_OLDEST = "OLDEST";

    private static final List<String> priorityGenreNamesOfMale = List.of(
            "fantasy", "modernFantasy", "wuxia", "drama", "mystery", "lightNovel", "romance", "romanceFantasy", "BL"
    );
    private static final List<String> priorityGenreNamesOfFemale = List.of(
            "romance", "romanceFantasy", "fantasy", "modernFantasy", "wuxia", "drama", "mystery", "lightNovel", "BL"
    );

    @Transactional(readOnly = true)
    public UserNovel getUserNovelOrException(User user, Long novelId) {
        return userNovelRepository.findByNovel_NovelIdAndUser(novelId, user)
                .orElseThrow(() -> new CustomUserNovelException(USER_NOVEL_NOT_FOUND,
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

        Optional<UserNovel> conflictingUserNovel = userNovelRepository.findByUserAndNovelIncludeDeleted(
                user.getUserId(), novel.getNovelId());

        if (conflictingUserNovel.isPresent()) {
            UserNovel userNovel = conflictingUserNovel.get();
            if (!userNovel.isDeleted()) {
                throw new CustomUserNovelException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
            }
            userNovel.restore();
            userNovel.updateUserNovel(request.userNovelRating(), request.status(), request.startDate(),
                    request.endDate());
            createUserNovelAttractivePoints(userNovel, request.attractivePoints());
            createNovelKeywords(userNovel, request.keywordIds());
            return;
        }

        try {
            UserNovel userNovel = userNovelRepository.save(UserNovel.create(
                    request.status(),
                    request.userNovelRating(),
                    request.startDate(),
                    request.endDate(),
                    user,
                    novel));

            createUserNovelAttractivePoints(userNovel, request.attractivePoints());
            createNovelKeywords(userNovel, request.keywordIds());
        } catch (DataIntegrityViolationException e) {
            throw new CustomUserNovelException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
        }
    }

    public void updateEvaluation(User user, Long novelId, UserNovelUpdateRequest request) {
        UserNovel userNovel = getUserNovelOrException(user, novelId);
        updateUserNovel(userNovel, request);
        updateAssociations(userNovel, request);
    }

    private void updateUserNovel(UserNovel userNovel, UserNovelUpdateRequest request) {
        userNovel.updateUserNovel(request.userNovelRating(), request.status(), request.startDate(), request.endDate());
    }

    private void updateAssociations(UserNovel userNovel, UserNovelUpdateRequest request) {
        updateAttractivePoints(userNovel, request.attractivePoints());
        updateKeywords(userNovel, request.keywordIds());
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

    private void addUserNovelAttractivePoints(UserNovel userNovel,
                                              Map<AttractivePoint, UserNovelAttractivePoint> currentPointMap,
                                              Set<AttractivePoint> requestedPoints) {
        for (AttractivePoint requested : requestedPoints) {
            if (!currentPointMap.containsKey(requested)) {
                userNovelAttractivePointRepository.save(UserNovelAttractivePoint.create(userNovel, requested));
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
                userNovelKeywordRepository.save(UserNovelKeyword.create(userNovel, requested));
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

    public void deleteEvaluation(User user, Long novelId) {
        UserNovel userNovel = getUserNovelOrException(user, novelId);

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
        Optional<UserNovel> conflictingUserNovel = userNovelRepository.findByUserAndNovelIncludeDeleted(
                user.getUserId(), novel.getNovelId());
        if (conflictingUserNovel.isPresent()) {
            UserNovel userNovel = conflictingUserNovel.get();
            if (!userNovel.isDeleted()) {
                throw new CustomUserNovelException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
            }
            userNovel.restore();
            return userNovel;
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
    public UserNovelCountGetResponse getUserNovelStatistics(Long ownerId) {
        return userNovelRepository.findUserNovelStatistics(ownerId);
    }

    @Transactional(readOnly = true)
    public UserNovelAndNovelsGetResponse getUserNovelsAndNovels(User visitor, Long ownerId, Boolean isInterest,
                                                                List<String> readStatuses,
                                                                List<String> attractivePoints, Float novelRating,
                                                                String query, Long lastUserNovelId, int size,
                                                                SortCriteria sortCriteria, LocalDateTime updatedSince) {
        User owner = userService.getUserOrException(ownerId);

        if (isProfileInaccessible(visitor, ownerId, owner)) {
            throw new CustomUserException(PRIVATE_PROFILE_STATUS, "the profile status of the user is set to private");
        }

        boolean isOwner = visitor.getUserId().equals(ownerId);
        boolean isAscending = sortCriteria.isOld();

        List<UserNovel> userNovels = userNovelRepository.findFilteredUserNovels(ownerId, isInterest, readStatuses,
                attractivePoints, novelRating, query, lastUserNovelId, size, isAscending, updatedSince);

        Long totalCount = userNovelRepository.countByUserIdAndFilters(ownerId, isInterest, readStatuses,
                attractivePoints, novelRating, query, updatedSince);

        boolean isLoadable = userNovels.size() == size;

        List<UserNovelAndNovelGetResponse> userNovelAndNovelGetResponses = buildUserNovelAndNovelGetResponses(
                userNovels, ownerId, isOwner);

        return new UserNovelAndNovelsGetResponse(totalCount, isLoadable, userNovelAndNovelGetResponses);
    }

    private List<UserNovelAndNovelGetResponse> buildUserNovelAndNovelGetResponses(List<UserNovel> userNovels,
                                                                                  Long ownerId, boolean isOwner) {
        Map<Long, List<String>> feedMap = getFeedsGroupedByNovel(userNovels, ownerId, isOwner);

        return userNovels.stream()
                .map(userNovel -> {
                    Long novelId = userNovel.getNovel().getNovelId();
                    Integer novelRatingCount = userNovelRepository.countByNovelAndUserNovelRatingNot(
                            userNovel.getNovel(), 0.0f);
                    Float novelRatingAvg = novelRatingCount == 0
                            ? 0.0f
                            : roundToFirstDecimal(userNovelRepository.sumUserNovelRatingByNovel(userNovel.getNovel())
                                    / novelRatingCount);
                    List<String> feeds = feedMap.getOrDefault(novelId, List.of());
                    return UserNovelAndNovelGetResponse.from(userNovel, novelRatingAvg, feeds);
                })
                .toList();
    }

    private Map<Long, List<String>> getFeedsGroupedByNovel(List<UserNovel> userNovels, Long ownerId, boolean isOwner) {
        List<Long> novelIds = userNovels.stream()
                .map(un -> un.getNovel().getNovelId())
                .distinct()
                .toList();

        List<Feed> feeds = isOwner
                ? feedRepository.findByUserUserIdAndIsHiddenFalseAndNovelIdIn(ownerId, novelIds)
                : feedRepository.findByUserUserIdAndIsHiddenFalseAndNovelIdInAndIsPublicTrueAndIsSpoilerFalse(ownerId,
                        novelIds);

        return feeds.stream()
                .collect(Collectors.groupingBy(Feed::getNovelId,
                        Collectors.mapping(Feed::getFeedContent, Collectors.toList())));
    }

    private float roundToFirstDecimal(float value) {
        return Math.round(value * 10.0f) / 10.0f;
    }

    @Transactional(readOnly = true)
    public UserGenrePreferencesGetResponse getUserGenrePreferences(User visitor, Long ownerId) {
        User owner = userService.getUserOrException(ownerId);

        if (isProfileInaccessible(visitor, ownerId, owner)) {
            throw new CustomUserException(PRIVATE_PROFILE_STATUS, "the profile status of the user is set to private");
        }

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

    private static boolean isOwner(User visitor, Long ownerId) {
        //TODO 현재는 비로그인 회원인 경우
        return visitor != null && visitor.getUserId().equals(ownerId);
    }

    private List<Genre> getPriorityOrderByGender(Gender gender, List<Genre> allGenres) {
        List<String> priorityGenreNames = gender.equals(M)
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

    @Transactional(readOnly = true)
    public UserTasteAttractivePointPreferencesAndKeywordsGetResponse getUserAttractivePointsAndKeywords(User visitor,
                                                                                                        Long ownerId) {
        User owner = userService.getUserOrException(ownerId);

        if (isProfileInaccessible(visitor, ownerId, owner)) {
            throw new CustomUserException(PRIVATE_PROFILE_STATUS, "the profile status of the user is set to private");
        }

        List<UserNovel> ownerUserNovels = userNovelRepository.findUserNovelByUser(owner);

        Map<String, Long> ownerAttractivePointCountMap = ownerUserNovels.stream()
                .map(UserNovel::getUserNovelAttractivePoints)
                .flatMap(List::stream)
                .map(UserNovelAttractivePoint::getAttractivePoint)
                .collect(Collectors.groupingBy(AttractivePoint::getAttractivePointName, Collectors.counting()));

        List<String> top3OwnerAttractivePointNames = ownerAttractivePointCountMap.entrySet()
                .stream()
                .sorted(Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Entry::getKey)
                .toList();

        Map<String, Long> tasteAttractivePoints = ownerUserNovels.stream()
                .map(UserNovel::getUserNovelKeywords)
                .flatMap(List::stream)
                .map(UserNovelKeyword::getKeyword)
                .collect(Collectors.groupingBy(Keyword::getKeywordName, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        List<TasteKeywordGetResponse> tasteKeywordGetResponses = tasteAttractivePoints.entrySet()
                .stream()
                .map(TasteKeywordGetResponse::of)
                .toList();

        return UserTasteAttractivePointPreferencesAndKeywordsGetResponse.of(
                top3OwnerAttractivePointNames, tasteKeywordGetResponses);
    }

    private static boolean isProfileInaccessible(User visitor, Long ownerId, User owner) {
        return !owner.getIsProfilePublic() && !isOwner(visitor, ownerId);
    }
}
