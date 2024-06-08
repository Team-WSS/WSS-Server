package org.websoso.WSSServer.dto.block;

import java.util.List;

public record BlocksGetResponse(
        List<BlockGetResponse> blocks
) {
}
