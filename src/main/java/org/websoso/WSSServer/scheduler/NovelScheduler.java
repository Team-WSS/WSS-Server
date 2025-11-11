package org.websoso.WSSServer.scheduler;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.PopularNovel;
import org.websoso.WSSServer.repository.PopularNovelRepository;
import org.websoso.WSSServer.library.repository.UserNovelRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class NovelScheduler {

    private final UserNovelRepository userNovelRepository;
    private final PopularNovelRepository popularNovelRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updatePopularNovels() {
        List<PopularNovel> yesterdayScheduledPopularNovels = popularNovelRepository.findAll();

        List<Long> topNovelIds = userNovelRepository.findTodayPopularNovelsId(PageRequest.of(0, 30));
        List<PopularNovel> popularNovels = topNovelIds.stream()
                .map(PopularNovel::from)
                .collect(Collectors.toList());

        popularNovelRepository.saveAll(popularNovels);
        popularNovelRepository.deleteAll(yesterdayScheduledPopularNovels);
    }
}
