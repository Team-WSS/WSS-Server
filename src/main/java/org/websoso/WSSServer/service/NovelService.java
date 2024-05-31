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
import org.websoso.WSSServer.dto.novel.NovelGetResponse;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.NovelStatisticsRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
public class NovelService {
    private final NovelRepository novelRepository;
    private final UserNovelRepository userNovelRepository;
    private final NovelStatisticsRepository novelStatisticsRepository;

    public NovelGetResponse getNovelInfo1(User user, Long novelId) {
        Novel novel = novelRepository.findByNovelIdOrThrow(novelId);
        UserNovel userNovel = userNovelRepository.findUserNovelByNovelAndUserOrThrow(novel, user);
        // TODO 유저노벨 없는 경우 처리하는 코드 추가
        NovelStatistics novelStatistics = novelStatisticsRepository.findByNovelOrThrow(novel);
        List<NovelGenre> novelGenres = novel.getNovelGenres();
        String novelGenreNames = getNovelGenreNames(novelGenres);
        String randomNovelGenreImage = getRandomNovelGenreImage(novelGenres);
        return NovelGetResponse.of(novel, userNovel, novelStatistics, novelGenreNames, randomNovelGenreImage);
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
