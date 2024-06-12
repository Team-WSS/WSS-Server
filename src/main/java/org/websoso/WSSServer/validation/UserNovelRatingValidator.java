package org.websoso.WSSServer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserNovelRatingValidator implements ConstraintValidator<UserNovelRatingConstraint, Float> {

    @Override
    public void initialize(UserNovelRatingConstraint userNovelRating) {
    }

    @Override
    public boolean isValid(Float value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        return value >= 0.0 && value <= 5.0 && (value * 10) % 5 == 0;
    }
}
