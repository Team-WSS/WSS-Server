package org.websoso.WSSServer.application;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.PopularNovel;
import org.websoso.WSSServer.novel.service.PopularNovelService;

@Service
@Transactional
@RequiredArgsConstructor
public class PopularNovelScheduler {

    private final PopularNovelService popularNovelService;
    private final LibraryService libraryService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updatePopularNovels() {
        List<PopularNovel> yesterdayScheduledPopularNovels = popularNovelService.getPopularNovels();

        List<Long> topNovelIds = libraryService.getTodayPopularNovelIds(PageRequest.of(0, 30));

        List<PopularNovel> popularNovels = topNovelIds.stream()
                .map(PopularNovel::from)
                .collect(Collectors.toList());

        popularNovelService.saveAll(popularNovels);
        popularNovelService.deleteAll(yesterdayScheduledPopularNovels);
    }
}
