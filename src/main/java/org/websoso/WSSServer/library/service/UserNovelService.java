package org.websoso.WSSServer.library.service;

import static org.websoso.WSSServer.domain.common.Gender.M;
import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.PRIVATE_PROFILE_STATUS;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.library.domain.AttractivePoint;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelGenre;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelAttractivePoint;
import org.websoso.WSSServer.library.domain.UserNovelKeyword;
import org.websoso.WSSServer.domain.common.Gender;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;
import org.websoso.WSSServer.dto.userNovel.TasteKeywordGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserGenrePreferenceGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserGenrePreferencesGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelAndNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelAndNovelGetResponseLegacy;
import org.websoso.WSSServer.dto.userNovel.UserNovelAndNovelsGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelAndNovelsGetResponseLegacy;
import org.websoso.WSSServer.dto.userNovel.UserTasteAttractivePointPreferencesAndKeywordsGetResponse;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.repository.GenreRepository;
import org.websoso.WSSServer.library.repository.UserNovelRepository;
import org.websoso.WSSServer.user.service.UserService;

// TODO: 남아있는 코드는 User Controller에서 사용중이라 일단 유지
@Service
@RequiredArgsConstructor
@Transactional
public class UserNovelService {

    private final UserNovelRepository userNovelRepository;
    private final UserService userService;
    private final GenreRepository genreRepository;
    private final FeedRepository feedRepository;

    private static final List<String> priorityGenreNamesOfMale = List.of(
            "fantasy", "modernFantasy", "wuxia", "drama", "mystery", "lightNovel", "romance", "romanceFantasy", "BL"
    );
    private static final List<String> priorityGenreNamesOfFemale = List.of(
            "romance", "romanceFantasy", "fantasy", "modernFantasy", "wuxia", "drama", "mystery", "lightNovel", "BL"
    );

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

    @Transactional(readOnly = true)
    public UserNovelAndNovelsGetResponseLegacy getUserNovelsAndNovelsLegacy(User visitor, Long ownerId,
                                                                            String readStatus,
                                                                            Long lastUserNovelId, int size,
                                                                            SortCriteria sortCriteria) {
        User owner = userService.getUserOrException(ownerId);

        if (isProfileInaccessible(visitor, ownerId, owner)) {
            throw new CustomUserException(PRIVATE_PROFILE_STATUS, "the profile status of the user is set to private");
        }

        boolean isAscending = sortCriteria.isOld();
        List<String> readStatuses = null;
        Boolean isInterest = null;

        if ("INTEREST".equalsIgnoreCase(readStatus)) {
            isInterest = true;
        } else {
            readStatuses = List.of(readStatus);
        }

        List<UserNovel> userNovels = userNovelRepository.findFilteredUserNovels(ownerId, isInterest, readStatuses,
                null, null, null, lastUserNovelId, size, isAscending, null);

        Long totalCount = userNovelRepository.countByUserIdAndFilters(ownerId, isInterest, readStatuses,
                null, null, null, null);

        boolean isLoadable = userNovels.size() == size;

        List<UserNovelAndNovelGetResponseLegacy> userNovelAndNovelGetResponseLegacies = userNovels.stream()
                .map(UserNovelAndNovelGetResponseLegacy::of)
                .toList();

        return new UserNovelAndNovelsGetResponseLegacy(totalCount, isLoadable, userNovelAndNovelGetResponseLegacies);
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
