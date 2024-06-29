package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.userNovel.UserNovelErrorCode.USER_NOVEL_ALREADY_EXISTS;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelGenre;
import org.websoso.WSSServer.domain.NovelKeywords;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.UserStatistics;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.exception.userNovel.exception.NovelAlreadyRegisteredException;
import org.websoso.WSSServer.repository.AttractivePointRepository;
import org.websoso.WSSServer.repository.NovelKeywordsRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserNovelService {

    private final UserNovelRepository userNovelRepository;
    private final NovelKeywordsRepository novelKeywordsRepository;
    private final AttractivePointRepository attractivePointRepository;
    private final NovelService novelService;
    private final UserStatisticsService userStatisticsService;
    private final NovelStatisticsService novelStatisticsService;
    private final KeywordService keywordService;

    public void createUserNovel(User user, UserNovelCreateRequest request) {

        Novel novel = novelService.getNovelOrException(request.novelId());

        if (getUserNovelOrNull(user, novel) != null) {
            throw new NovelAlreadyRegisteredException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
        }

        UserNovel userNovel = userNovelRepository.save(UserNovel.create(
                request.status(),
                request.userNovelRating(),
                request.startDate() != null ? convertToLocalDate(request.startDate()) : null,
                request.endDate() != null ? convertToLocalDate(request.endDate()) : null,
                user,
                novel));

        AttractivePoint attractivePoint = attractivePointRepository.save(AttractivePoint.create(userNovel));
        AttractivePointService.setAttractivePoint(attractivePoint, request.attractivePoints());

        for (Integer keywordId : request.keywordIds()) {
            Keyword keyword = keywordService.getKeywordOrException(keywordId);
            novelKeywordsRepository.save(
                    NovelKeywords.create(novel.getNovelId(), keyword.getKeywordId(), user.getUserId()));
        }

        increaseStatistics(user, novel, request, attractivePoint);
    }

    public UserNovel createUserNovelByInterest(User user, Novel novel) {

        if (getUserNovelOrNull(user, novel) != null) {
            throw new NovelAlreadyRegisteredException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
        }

        UserNovel userNovel = userNovelRepository.save(UserNovel.create(null, null, null, null, user, novel));

        attractivePointRepository.save(AttractivePoint.create(userNovel));

        return userNovel;
    }

    @Transactional(readOnly = true)
    public UserNovel getUserNovelOrNull(User user, Novel novel) {
        if (user == null) {
            return null;
        }
        return userNovelRepository.findByNovelAndUser(novel, user).orElse(null);
    }

    private LocalDate convertToLocalDate(String string) {
        return LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
    }

    private void increaseStatistics(User user, Novel novel, UserNovelCreateRequest request,
                                    AttractivePoint attractivePoint) {
        UserStatistics userStatistics = userStatisticsService.getUserStatisticsOrException(user);
        NovelStatistics novelStatistics = novelStatisticsService.getNovelStatisticsOrException(novel);

        increaseStatisticsByReadStatus(request.status(), userStatistics, novelStatistics);
        increaseStatisticsByAttractivePoint(attractivePoint, novelStatistics);
        if (request.userNovelRating() != 0.0f) {
            novel.increaseNovelRatingCount();
            novel.increaseNovelRatingSum(request.userNovelRating());
            increaseStatisticsByNovelGenre(novel.getNovelGenres(), userStatistics);
        }
    }

    private void increaseStatisticsByReadStatus(ReadStatus readStatus, UserStatistics userStatistics,
                                                NovelStatistics novelStatistics) {
        switch (readStatus) {
            case WATCHING -> {
                userStatistics.increaseWatchingNovelCount();
                novelStatistics.increaseWatchingCount();
            }
            case WATCHED -> {
                userStatistics.increaseWatchedNovelCount();
                novelStatistics.increaseWatchedCount();
            }
            case QUIT -> {
                userStatistics.increaseQuitNovelCount();
                novelStatistics.increaseQuitCount();
            }
        }
    }

    private void increaseStatisticsByNovelGenre(List<NovelGenre> novelGenres, UserStatistics userStatistics) {
        for (NovelGenre novelGenre : novelGenres) {
            switch (novelGenre.getGenre().getGenreName()) {
                case "로맨스" -> userStatistics.increaseRoNovelNovelCount();
                case "로판" -> userStatistics.increaseRfNovelNovelCount();
                case "BL" -> userStatistics.increaseBlNovelNovelCount();
                case "판타지" -> userStatistics.increaseFaNovelNovelCount();
                case "현판" -> userStatistics.increaseMfNovelNovelCount();
                case "무협" -> userStatistics.increaseWuNovelNovelCount();
                case "라노벨" -> userStatistics.increaseLnNovelNovelCount();
                case "드라마" -> userStatistics.increaseDrNovelNovelCount();
                case "미스터리" -> userStatistics.increaseMyNovelNovelCount();
            }
        }
    }

    private void increaseStatisticsByAttractivePoint(AttractivePoint attractivePoint, NovelStatistics novelStatistics) {
        if (attractivePoint.getUniverse()) {
            novelStatistics.increaseUniverseCount();
        }
        if (attractivePoint.getVibe()) {
            novelStatistics.increaseVibeCount();
        }
        if (attractivePoint.getMaterial()) {
            novelStatistics.increaseMaterialCount();
        }
        if (attractivePoint.getCharacters()) {
            novelStatistics.increaseCharactersCount();
        }
        if (attractivePoint.getRelationship()) {
            novelStatistics.increaseRelationshipCount();
        }
    }

}
