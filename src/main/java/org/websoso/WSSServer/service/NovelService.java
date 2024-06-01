package org.websoso.WSSServer.service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelGenre;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.dto.novel.NovelGetResponse1;
import org.websoso.WSSServer.exception.novel.NovelErrorCode;
import org.websoso.WSSServer.exception.novel.exception.InvalidNovelException;
import org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode;
import org.websoso.WSSServer.exception.novelStatistics.exception.InvalidNovelStatisticsException;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.NovelStatisticsRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
public class NovelService {
    private final NovelRepository novelRepository;
    private final UserNovelRepository userNovelRepository;
    private final NovelStatisticsRepository novelStatisticsRepository;

    public NovelGetResponse1 getNovelInfo1(User user, Long novelId) {
        Novel novel = getNovelOrException(novelId);
        NovelStatistics novelStatistics = getNovelStatisticsOrException(novel);
        UserNovel userNovel = getUserNovelOrNull(user, novel);
        List<NovelGenre> novelGenres = novel.getNovelGenres();
        String novelGenreNames = getNovelGenreNames(novelGenres);
        String randomNovelGenreImage = getRandomNovelGenreImage(novelGenres);
        return NovelGetResponse1.of(novel, userNovel, novelStatistics, novelGenreNames, randomNovelGenreImage);
    }

    private Novel getNovelOrException(Long novelId) {
        return novelRepository.findById(novelId).orElseThrow(
                () -> new InvalidNovelException(NovelErrorCode.NOVEL_NOT_FOUND,
                        "novel with the given id is not found"));
    }

    private NovelStatistics getNovelStatisticsOrException(Novel novel) {
        return novelStatisticsRepository.findByNovel(novel).orElseThrow(
                () -> new InvalidNovelStatisticsException(NovelStatisticsErrorCode.NOVEL_STATISTICS_NOT_FOUND,
                        "novel statistics with the given novel is not found"));
    }

    private UserNovel getUserNovelOrNull(User user, Novel novel) {
        return userNovelRepository.findByNovelAndUser(novel, user).orElse(null);
    }

    private String getNovelGenreNames(List<NovelGenre> novelGenres) {
        return novelGenres.stream().map(novelGenre -> novelGenre.getGenre().getGenreName())
                .collect(Collectors.joining(", "));
    }

    private String getRandomNovelGenreImage(List<NovelGenre> novelGenres) {
        Random random = new Random();
        return novelGenres.get(random.nextInt(novelGenres.size())).getGenre().getGenreImage();
    }

}
