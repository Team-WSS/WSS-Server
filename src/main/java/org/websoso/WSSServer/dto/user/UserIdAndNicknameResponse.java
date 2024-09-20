package org.websoso.WSSServer.dto.user;

import org.websoso.WSSServer.domain.User;

public record UserIdAndNicknameResponse(
        Long userId,
        String nickname
) {

    public static UserIdAndNicknameResponse of(User user) {
        return new UserIdAndNicknameResponse(
                user.getUserId(),
                user.getNickname()
        );
    }
}
