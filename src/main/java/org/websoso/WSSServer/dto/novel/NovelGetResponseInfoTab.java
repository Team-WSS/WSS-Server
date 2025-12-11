package org.websoso.WSSServer.dto.novel;

import java.util.List;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.dto.keyword.KeywordCountGetResponse;
import org.websoso.WSSServer.dto.platform.PlatformGetResponse;

public record NovelGetResponseInfoTab(
        String novelDescription,
        List<PlatformGetResponse> platforms,
        List<String> attractivePoints,
        List<KeywordCountGetResponse> keywords,
        Integer watchingCount,
        Integer watchedCount,
        Integer quitCount
) {
    public static NovelGetResponseInfoTab of(Novel novel, List<PlatformGetResponse> platforms, List<String> attractivePoints,
                                             List<KeywordCountGetResponse> keywords, Integer watchingCount,
                                             Integer watchedCount, Integer quitCount) {
        return new NovelGetResponseInfoTab(
                novel.getNovelDescription(),
                platforms,
                attractivePoints,
                keywords,
                watchingCount,
                watchedCount,
                quitCount
        );
    }
}
