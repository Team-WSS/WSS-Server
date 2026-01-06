package org.websoso.WSSServer.dto.platform;

import org.websoso.WSSServer.novel.domain.NovelPlatform;
import org.websoso.WSSServer.novel.domain.Platform;

public record PlatformGetResponse(
        String platformName,
        String platformImage,
        String platformUrl
) {
    public static PlatformGetResponse of(NovelPlatform novelPlatform) {
        Platform platform = novelPlatform.getPlatform();
        return new PlatformGetResponse(
                platform.getPlatformName(),
                platform.getPlatformImage(),
                novelPlatform.getNovelPlatformUrl()
        );
    }
}
