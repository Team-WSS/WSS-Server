package org.websoso.WSSServer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Novel;
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

    public NovelGetResponse getNovelInfo1(User user, Long novelId){
        Novel novel = novelRepository.findByNovelIdOrThrow(novelId);
        UserNovel userNovel = userNovelRepository.findUserNovelByNovelAndUserOrThrow(novel, user);
        NovelStatistics novelStatistics = novelStatisticsRepository.findByNovelOrThrow(novel);
        return NovelGetResponse.of(novel, userNovel, novelStatistics);
    }
}
