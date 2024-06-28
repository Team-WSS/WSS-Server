package org.websoso.WSSServer.dto.sosoPick;

import java.util.List;

public record SosoPickGetResponse(
        List<SosoPickNovelGetResponse> sosoPicks
) {
    public static SosoPickGetResponse of(List<SosoPickNovelGetResponse> sosoPicks) {
        return new SosoPickGetResponse(sosoPicks);
    }
}
