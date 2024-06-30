package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.novel.NovelErrorCode.NOVEL_NOT_FOUND;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelGenre;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.novel.NovelGetResponse1;
import org.websoso.WSSServer.exception.novel.exception.CustomNovelException;
import org.websoso.WSSServer.repository.NovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class NovelService {

    private final NovelRepository novelRepository;
    private final NovelStatisticsService novelStatisticsService;
    private final UserNovelService userNovelService;

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
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND,
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

}
