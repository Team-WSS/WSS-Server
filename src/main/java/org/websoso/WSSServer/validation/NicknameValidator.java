package org.websoso.WSSServer.validation;

import static org.websoso.WSSServer.exception.user.UserErrorCode.INVALID_NICKNAME_BLANK;
import static org.websoso.WSSServer.exception.user.UserErrorCode.INVALID_NICKNAME_LENGTH;
import static org.websoso.WSSServer.exception.user.UserErrorCode.INVALID_NICKNAME_NULL;
import static org.websoso.WSSServer.exception.user.UserErrorCode.INVALID_NICKNAME_PATTERN;
import static org.websoso.WSSServer.exception.user.UserErrorCode.INVALID_NICKNAME_START_OR_END_WITH_BLANK;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.exception.user.exception.InvalidNicknameException;

@Component
public class NicknameValidator implements ConstraintValidator<NicknameConstraint, String> {

    private static final String NICKNAME_REGEX = "^\\s*[가-힣a-zA-Z0-9]*\\s*$";

    @Override
    public void initialize(NicknameConstraint nickname) {
    }

    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext constraintValidatorContext) {
        if (nickname == null) {
            throw new InvalidNicknameException(INVALID_NICKNAME_NULL, "nickname cannot be null");
        }
        if (nickname.isBlank()) {
            throw new InvalidNicknameException(INVALID_NICKNAME_BLANK, "nickname cannot be blank");
        }
        if (!(nickname.length() >= 2 && nickname.length() <= 10)) {
            throw new InvalidNicknameException(INVALID_NICKNAME_LENGTH, "nickname must be 2 to 10 characters long");
        }
        if (!nickname.matches(NICKNAME_REGEX)) {
            throw new InvalidNicknameException(INVALID_NICKNAME_PATTERN,
                    "nickname should be written in Korean, English, and numbers");
        }
        if (nickname.startsWith(" ") || nickname.endsWith(" ")) {
            throw new InvalidNicknameException(INVALID_NICKNAME_START_OR_END_WITH_BLANK,
                    "nickname cannot start or end with whitespace");
        }
        return true;
    }
}