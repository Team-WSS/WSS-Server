package org.websoso.WSSServer.scheduler;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.RecentUserNovel;
import org.websoso.WSSServer.repository.RecentUserNovelRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class NovelScheduler {

    private final UserNovelRepository userNovelRepository;
    private final RecentUserNovelRepository recentUserNovelRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateRecentUserNovels() {
        List<Long> topNovelIds = userNovelRepository.findTodayPopularNovelsId(PageRequest.of(0, 30));
        List<RecentUserNovel> popularNovels = topNovelIds.stream()
                .map(popularNovelId -> {
                    return RecentUserNovel.from(popularNovelId);
                })
                .collect(Collectors.toList());

        recentUserNovelRepository.saveAll(popularNovels);
    }
}
