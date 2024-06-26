package org.websoso.WSSServer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ZeroAllowedUserIdValidator implements ConstraintValidator<ZeroAllowedUserIdConstraint, Long> {

    private final UserIdValidator userIdValidator;

    @Override
    public void initialize(ZeroAllowedUserIdConstraint userId) {
    }

    @Override
    public boolean isValid(Long userId, ConstraintValidatorContext constraintValidatorContext) {
        if (userId == 0) {
            return true;
        }
        return userIdValidator.isValid(userId, constraintValidatorContext);
    }
}
