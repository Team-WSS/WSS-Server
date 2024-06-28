package org.websoso.WSSServer.dto.platform;

import org.websoso.WSSServer.domain.Platform;

public record PlatformGetResponse(
        String platformName,
        String platformImage,
        String platformUrl
) {
    public static PlatformGetResponse of(Platform platform) {
        return new PlatformGetResponse(
                platform.getPlatformName(),
                platform.getPlatformImage(),
                platform.getPlatformUrl()
        );
    }
}
