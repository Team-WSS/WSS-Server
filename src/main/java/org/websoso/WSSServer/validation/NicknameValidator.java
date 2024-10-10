package org.websoso.WSSServer.validation;

import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_NICKNAME_BLANK;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_NICKNAME_CONTAINS_WHITESPACE;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_NICKNAME_LENGTH;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_NICKNAME_NULL;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_NICKNAME_PATTERN;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.exception.exception.CustomUserException;

@Component
public class NicknameValidator implements ConstraintValidator<NicknameConstraint, String> {

    private static final Pattern NICKNAME_REGEX = Pattern.compile("^\\s*[가-힣a-zA-Z0-9]*\\s*$");

    @Override
    public void initialize(NicknameConstraint nickname) {
    }

    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext constraintValidatorContext) {
        if (nickname == null) {
            throw new CustomUserException(INVALID_NICKNAME_NULL, "nickname cannot be null");
        }
        if (nickname.isBlank()) {
            throw new CustomUserException(INVALID_NICKNAME_BLANK, "nickname cannot be blank");
        }
        if (nickname.contains(" ")) {
            throw new CustomUserException(INVALID_NICKNAME_CONTAINS_WHITESPACE, "nickname cannot contain whitespace");
        }
        if (!(nickname.length() >= 2 && nickname.length() <= 10)) {
            throw new CustomUserException(INVALID_NICKNAME_LENGTH, "nickname must be 2 to 10 characters long");
        }
        if (!isMatches(nickname)) {
            throw new CustomUserException(INVALID_NICKNAME_PATTERN,
                    "nickname should be written in Korean, English, and numbers");
        }
        return true;
    }

    private static boolean isMatches(String nickname) {
        return NICKNAME_REGEX.matcher(nickname).matches();
    }
}
