package org.websoso.WSSServer.dto.user;

public record NicknameValidation(boolean isDuplicated) {
    public static NicknameValidation of(boolean isDuplicated) {
        return new NicknameValidation(isDuplicated);
    }
}
