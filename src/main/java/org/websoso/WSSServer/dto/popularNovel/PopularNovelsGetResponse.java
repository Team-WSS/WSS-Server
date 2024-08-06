package org.websoso.WSSServer.dto.popularNovel;

import java.util.List;

public record PopularNovelsGetResponse(
        List<PopularNovelGetResponse> popularNovels
) {
}
