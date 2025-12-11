package org.websoso.WSSServer.dto.user;

import org.websoso.WSSServer.user.domain.User;

public record UserIdAndNicknameResponse(
        Long userId,
        String nickname,
        String gender
) {

    public static UserIdAndNicknameResponse of(User user) {
        return new UserIdAndNicknameResponse(
                user.getUserId(),
                user.getNickname(),
                user.getGender().name()
        );
    }
}
