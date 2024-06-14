package org.websoso.WSSServer.dto.avatar;

import java.util.List;

public record AvatarsGetResponse(
        List<AvatarGetResponse> avatars
) {
}
