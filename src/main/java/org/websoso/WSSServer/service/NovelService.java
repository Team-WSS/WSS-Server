package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.genre.GenreErrorCode.GENRE_NOT_FOUND;
import static org.websoso.WSSServer.exception.novel.NovelErrorCode.ALREADY_INTERESTED;
import static org.websoso.WSSServer.exception.novel.NovelErrorCode.NOVEL_NOT_FOUND;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelGenre;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.UserStatistics;
import org.websoso.WSSServer.domain.common.Flag;
import org.websoso.WSSServer.dto.novel.NovelGetResponse1;
import org.websoso.WSSServer.exception.genre.exception.InvalidGenreException;
import org.websoso.WSSServer.exception.novel.exception.InvalidNovelException;
import org.websoso.WSSServer.repository.NovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class NovelService {

    private final NovelRepository novelRepository;
    private final NovelStatisticsService novelStatisticsService;
    private final UserNovelService userNovelService;
    private final UserStatisticsService userStatisticsService;

    @Transactional(readOnly = true)
    public NovelGetResponse1 getNovelInfo1(User user, Long novelId) {
        Novel novel = getNovelOrException(novelId);
        List<NovelGenre> novelGenres = novel.getNovelGenres();
        return NovelGetResponse1.of(
                novel,
                userNovelService.getUserNovelOrNull(user, novel),
                novelStatisticsService.getNovelStatisticsOrException(novel),
                getNovelGenreNames(novelGenres),
                getRandomNovelGenreImage(novelGenres)
        );
    }

    @Transactional(readOnly = true)
    public Novel getNovelOrException(Long novelId) {
        return novelRepository.findById(novelId)
                .orElseThrow(() -> new InvalidNovelException(NOVEL_NOT_FOUND,
                        "novel with the given id is not found"));
    }

    private String getNovelGenreNames(List<NovelGenre> novelGenres) {
        return novelGenres.stream().map(novelGenre -> novelGenre.getGenre().getGenreName())
                .collect(Collectors.joining("/"));
    }

    private String getRandomNovelGenreImage(List<NovelGenre> novelGenres) {
        Random random = new Random();
        return novelGenres.get(random.nextInt(novelGenres.size())).getGenre().getGenreImage();
    }

    public void registerAsInterest(User user, Long novelId) {

        Novel novel = getNovelOrException(novelId);
        UserNovel userNovel = userNovelService.getUserNovelOrNull(user, novel);

        if (userNovel != null && userNovel.getIsInterest() == Flag.Y) {
            throw new InvalidNovelException(ALREADY_INTERESTED, "already interested the novel");
        }

        if (userNovel == null) {
            userNovel = userNovelService.createUserNovelByInterest(user, novel);
        }

        NovelStatistics novelStatistics = novelStatisticsService.getNovelStatisticsOrException(novel);
        UserStatistics userStatistics = userStatisticsService.getUserStatisticsOrException(user);

        userNovel.setIsInterest(Flag.Y);
        novelStatistics.increaseInterestCount();
        userStatistics.increaseInterestNovelCount();

        List<String> genreNames = novel.getNovelGenres()
                .stream()
                .map(NovelGenre::getGenre)
                .map(Genre::getGenreName)
                .toList();

        for (String genreName : genreNames) {
            switch (genreName) {
                case "로맨스" -> userStatistics.increaseRoNovelNovelCount();
                case "로판" -> userStatistics.increaseRfNovelNovelCount();
                case "BL" -> userStatistics.increaseBlNovelNovelCount();
                case "판타지" -> userStatistics.increaseFaNovelNovelCount();
                case "현판" -> userStatistics.increaseMfNovelNovelCount();
                case "무협" -> userStatistics.increaseWuNovelNovelCount();
                case "라노벨" -> userStatistics.increaseLnNovelNovelCount();
                case "드라마" -> userStatistics.increaseDrNovelNovelCount();
                case "미스터리" -> userStatistics.increaseMyNovelNovelCount();
                default -> throw new InvalidGenreException(GENRE_NOT_FOUND, "cannot find corresponding genre");
            }
        }
    }

}
