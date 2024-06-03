package org.websoso.WSSServer.dto.novel;

import java.util.List;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.dto.Keyword.KeywordCountGetResponse;
import org.websoso.WSSServer.dto.platform.PlatformGetResponse;

public record NovelGetResponse2(
        String novelDescription,
        List<PlatformGetResponse> platforms,
        List<String> attractivePoints,
        List<KeywordCountGetResponse> keywords,
        Integer watchingCount,
        Integer watchedCount,
        Integer quitCount
) {
    public static NovelGetResponse2 of(Novel novel, NovelStatistics novelStatistics,
                                       List<PlatformGetResponse> platforms, List<String> attractivePoints,
                                       List<KeywordCountGetResponse> keywords) {
        return new NovelGetResponse2(
                novel.getNovelDescription(),
                platforms,
                attractivePoints,
                keywords,
                novelStatistics.getWatchingCount(),
                novelStatistics.getWatchedCount(),
                novelStatistics.getQuitCount()
        );
    }
}
