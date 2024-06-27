package org.websoso.WSSServer.dto.User;

public record UserBasicInfo(
        Long userId,
        String nickname,
        String avatarImage
) {
    public static UserBasicInfo of(Long userId, String nickname, String avatarImage) {
        return new UserBasicInfo(
                userId,
                nickname,
                avatarImage
        );
    }
}
