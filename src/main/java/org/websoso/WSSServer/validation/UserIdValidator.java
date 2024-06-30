package org.websoso.WSSServer.validation;

import static org.websoso.WSSServer.exception.user.UserErrorCode.INVALID_USER_ID;
import static org.websoso.WSSServer.exception.user.UserErrorCode.USER_NOT_FOUND;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.exception.user.exception.CustomUserException;
import org.websoso.WSSServer.repository.UserRepository;

@Component
@AllArgsConstructor
public class UserIdValidator implements ConstraintValidator<UserIdConstraint, Long> {

    private final UserRepository userRepository;

    @Override
    public void initialize(UserIdConstraint userId) {
    }

    @Override
    public boolean isValid(Long userId, ConstraintValidatorContext constraintValidatorContext) {
        if (userId <= 0) {
            throw new CustomUserException(INVALID_USER_ID, "given userId is an invalid value");
        }
        userRepository.findById(userId).orElseThrow(() ->
                new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        return true;
    }
}
