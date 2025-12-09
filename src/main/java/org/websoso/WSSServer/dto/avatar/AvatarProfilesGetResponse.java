package org.websoso.WSSServer.dto.avatar;

import java.util.List;

public record AvatarProfilesGetResponse(
        List<AvatarProfileGetResponse> avatarProfiles
) {

}
