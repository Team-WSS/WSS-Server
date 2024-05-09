package org.websoso.WSSServer.dto.User;

public record NicknameValidation(boolean isDuplicated) {
    public static NicknameValidation of(boolean isDuplicated) {
        return new NicknameValidation(isDuplicated);
    }
}