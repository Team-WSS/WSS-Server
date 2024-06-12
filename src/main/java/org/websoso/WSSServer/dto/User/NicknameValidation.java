package org.websoso.WSSServer.dto.User;

public record NicknameValidation(
        boolean isValid
) {

    public static NicknameValidation of(boolean isValid) {
        return new NicknameValidation(isValid);
    }
}
