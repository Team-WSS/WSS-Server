package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.keyword.KeywordErrorCode.KEYWORD_NOT_FOUND;
import static org.websoso.WSSServer.exception.novel.NovelErrorCode.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode.NOVEL_STATISTICS_NOT_FOUND;
import static org.websoso.WSSServer.exception.userNovel.UserNovelErrorCode.USER_NOVEL_ALREADY_EXISTS;
import static org.websoso.WSSServer.exception.userStatistics.UserStatisticsErrorCode.USER_STATISTICS_NOT_FOUND;

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
import org.websoso.WSSServer.domain.common.Flag;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.exception.keyword.exception.InvalidKeywordException;
import org.websoso.WSSServer.exception.novel.exception.InvalidNovelException;
import org.websoso.WSSServer.exception.novelStatistics.exception.InvalidNovelStatisticsException;
import org.websoso.WSSServer.exception.userNovel.exception.NovelAlreadyRegisteredException;
import org.websoso.WSSServer.exception.userStatistics.exception.InvalidUserStatisticsException;
import org.websoso.WSSServer.repository.KeywordRepository;
import org.websoso.WSSServer.repository.NovelKeywordsRepository;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.NovelStatisticsRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;
import org.websoso.WSSServer.repository.UserStatisticsRepository;

@Service
@RequiredArgsConstructor
public class UserNovelService {

    private final UserNovelRepository userNovelRepository;
    private final NovelRepository novelRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final NovelStatisticsRepository novelStatisticsRepository;
    private final NovelKeywordsRepository novelKeywordsRepository;
    private final KeywordRepository keywordRepository;

    @Transactional
    public void createUserNovel(User user, UserNovelCreateRequest request) {

        Novel novel = novelRepository.findById(request.novelId())
                .orElseThrow(() -> new InvalidNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"));

        if (getUserNovelOrNull(user, novel) != null) {
            throw new NovelAlreadyRegisteredException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
        }

        UserNovel userNovel = userNovelRepository.save(UserNovel.create(
                request.status(),
                request.userNovelRating(),
                request.startDate() != null ? convertToLocalDate(request.startDate()) : null,
                request.endDate() != null ? convertToLocalDate(request.endDate()) : null,
                null,
                user,
                novel));

        AttractivePoint attractivePoint = AttractivePointService.createAndGetAttractivePoint(request.attractivePoints(),
                userNovel);
        userNovel.setAttractivePoint(attractivePoint);

        for (Integer keywordId : request.keywordIds()) {
            Keyword keyword = keywordRepository.findById(keywordId).orElseThrow(
                    () -> new InvalidKeywordException(KEYWORD_NOT_FOUND, "keyword with the given id is not found"));
            novelKeywordsRepository.save(
                    NovelKeywords.create(novel.getNovelId(), keyword.getKeywordId(), user.getUserId()));
        }

        increaseStatistics(user, novel, request, attractivePoint);
    }

    protected UserNovel getUserNovelOrNull(User user, Novel novel) {
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
        UserStatistics userStatistics = userStatisticsRepository.findByUser(user).orElseThrow(
                () -> new InvalidUserStatisticsException(USER_STATISTICS_NOT_FOUND,
                        "user statistics with the given user is not found"));
        NovelStatistics novelStatistics = novelStatisticsRepository.findByNovel(novel).orElseThrow(
                () -> new InvalidNovelStatisticsException(NOVEL_STATISTICS_NOT_FOUND,
                        "novel statistics with the given novel is not found"));

        increaseStatisticsByReadStatus(request.status(), userStatistics, novelStatistics);
        increaseStatisticsByAttractivePoint(attractivePoint, novelStatistics);
        if (request.userNovelRating() != 0.0f) {
            novel.increaseNovelRatingCount();
            novel.increaseNovelRatingSum(request.userNovelRating());
            increaseStatisticsByNovelGenre(novel.getNovelGenres(), request.userNovelRating(), userStatistics);
        }
    }

    private static void increaseStatisticsByReadStatus(ReadStatus readStatus, UserStatistics userStatistics,
                                                       NovelStatistics novelStatistics) {
        switch (readStatus) {
            case WATCHING -> {
                userStatistics.increaseField("watchingNovelCount");
                novelStatistics.increaseField("watchingCount");
            }
            case WATCHED -> {
                userStatistics.increaseField("watchedNovelCount");
                novelStatistics.increaseField("watchedCount");
            }
            case QUIT -> {
                userStatistics.increaseField("quitNovelCount");
                novelStatistics.increaseField("quitCount");
            }
        }
    }

    private static void increaseStatisticsByNovelGenre(List<NovelGenre> novelGenres, Float userNovelRating,
                                                       UserStatistics userStatistics) {
        for (NovelGenre novelGenre : novelGenres) {
            switch (novelGenre.getGenre().getGenreName()) {
                case "로맨스" -> {
                    userStatistics.increaseField("roNovelNovelCount");
                    userStatistics.increaseFieldByAmount("roNovelRatingSum", userNovelRating);
                }
                case "로판" -> {
                    userStatistics.increaseField("rfNovelNovelCount");
                    userStatistics.increaseFieldByAmount("rfNovelRatingSum", userNovelRating);
                }
                case "BL" -> {
                    userStatistics.increaseField("blNovelNovelCount");
                    userStatistics.increaseFieldByAmount("blNovelRatingSum", userNovelRating);
                }
                case "판타지" -> {
                    userStatistics.increaseField("faNovelNovelCount");
                    userStatistics.increaseFieldByAmount("faNovelRatingSum", userNovelRating);
                }
                case "현판" -> {
                    userStatistics.increaseField("mfNovelNovelCount");
                    userStatistics.increaseFieldByAmount("mfNovelRatingSum", userNovelRating);
                }
                case "무협" -> {
                    userStatistics.increaseField("wuNovelNovelCount");
                    userStatistics.increaseFieldByAmount("wuNovelRatingSum", userNovelRating);
                }
                case "라노벨" -> {
                    userStatistics.increaseField("lnNovelNovelCount");
                    userStatistics.increaseFieldByAmount("lnNovelRatingSum", userNovelRating);
                }
                case "드라마" -> {
                    userStatistics.increaseField("drNovelNovelCount");
                    userStatistics.increaseFieldByAmount("drNovelRatingSum", userNovelRating);
                }
                case "미스터리" -> {
                    userStatistics.increaseField("myNovelNovelCount");
                    userStatistics.increaseFieldByAmount("myNovelNovelCount", userNovelRating);
                }
            }
        }
    }

    private static void increaseStatisticsByAttractivePoint(AttractivePoint attractivePoint,
                                                            NovelStatistics novelStatistics) {
        if (attractivePoint.getUniverse() == Flag.Y) {
            novelStatistics.increaseField("universeCount");
        }
        if (attractivePoint.getVibe() == Flag.Y) {
            novelStatistics.increaseField("vibeCount");
        }
        if (attractivePoint.getMaterial() == Flag.Y) {
            novelStatistics.increaseField("materialCount");
        }
        if (attractivePoint.getCharacters() == Flag.Y) {
            novelStatistics.increaseField("charactersCount");
        }
        if (attractivePoint.getRelationship() == Flag.Y) {
            novelStatistics.increaseField("relationshipCount");
        }
    }

}
