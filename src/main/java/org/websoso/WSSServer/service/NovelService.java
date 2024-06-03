package org.websoso.WSSServer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelGenre;
import org.websoso.WSSServer.domain.NovelKeywords;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.domain.Platform;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.dto.Keyword.KeywordCountGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponse1;
import org.websoso.WSSServer.dto.novel.NovelGetResponse2;
import org.websoso.WSSServer.dto.platform.PlatformGetResponse;
import org.websoso.WSSServer.exception.novel.NovelErrorCode;
import org.websoso.WSSServer.exception.novel.exception.InvalidNovelException;
import org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode;
import org.websoso.WSSServer.exception.novelStatistics.exception.InvalidNovelStatisticsException;
import org.websoso.WSSServer.exception.platform.PlatformErrorCode;
import org.websoso.WSSServer.exception.platform.exception.InvalidPlatformException;
import org.websoso.WSSServer.repository.KeywordRepository;
import org.websoso.WSSServer.repository.NovelKeywordRepository;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.NovelStatisticsRepository;
import org.websoso.WSSServer.repository.PlatformRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
public class NovelService {
    private final NovelRepository novelRepository;
    private final UserNovelRepository userNovelRepository;
    private final NovelStatisticsRepository novelStatisticsRepository;
    private final PlatformRepository platformRepository;
    private final KeywordRepository keywordRepository;
    private final NovelKeywordRepository novelKeywordRepository;

    public NovelGetResponse1 getNovelInfo1(User user, Long novelId) {
        Novel novel = getNovelOrException(novelId);
        NovelStatistics novelStatistics = getNovelStatisticsOrException(novel);
        UserNovel userNovel = getUserNovelOrNull(user, novel);
        List<NovelGenre> novelGenres = novel.getNovelGenres();
        String novelGenreNames = getNovelGenreNames(novelGenres);
        String randomNovelGenreImage = getRandomNovelGenreImage(novelGenres);
        return NovelGetResponse1.of(novel, userNovel, novelStatistics, novelGenreNames, randomNovelGenreImage);
    }

    public NovelGetResponse2 getNovelInfo2(Long novelId) {
        Novel novel = getNovelOrException(novelId);
        NovelStatistics novelStatistics = getNovelStatisticsOrException(novel);
        return NovelGetResponse2.of(
                novel,
                novelStatistics,
                getPlatformResponses(novel),
                getAttractivePoints(novelStatistics),
                getKeywordResponses(novelId)
        );
    }

    private List<PlatformGetResponse> getPlatformResponses(Novel novel) {
        List<Platform> platforms = platformRepository.findAllByNovel(novel);
        if (platforms.isEmpty()) {
            throw new InvalidPlatformException(PlatformErrorCode.PLATFORM_NOT_FOUND,
                    "platform with the given novel is not found");
        }
        return platforms.stream().map(PlatformGetResponse::of).collect(Collectors.toList());
    }

    private List<String> getAttractivePoints(NovelStatistics novelStatistics) { //TODO
    }

    private List<KeywordCountGetResponse> getKeywordResponses(Long novelId) {
        //TODO 아예 없는 경우 : 빈배열
        //TODO 등록한 개수가 많은 5개 키워드를 순서대로 노출
        List<NovelKeywords> novelKeywords = novelKeywordRepository.findAllByNovelId(novelId);
        Map<Integer, Long> keywordFrequencyMap = novelKeywords.stream()
                .collect(Collectors.groupingBy(NovelKeywords::getKeywordId, Collectors.counting()));
        List<Integer> keywordIds = new ArrayList<>(keywordFrequencyMap.keySet());
        List<Keyword> keywords = keywordRepository.findAllById(keywordIds);
        return keywords.stream()
                .map(keyword -> KeywordCountGetResponse.of(
                        keyword,
                        keywordFrequencyMap.getOrDefault(keyword.getKeywordId(), 0L).intValue()))
                .collect(Collectors.toList());
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
