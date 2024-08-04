package org.websoso.WSSServer.validation;

import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_GENDER;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_GENDER_BLANK;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_GENDER_NULL;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.domain.common.Gender;
import org.websoso.WSSServer.exception.exception.CustomUserException;

@Component
@AllArgsConstructor
public class GenderValidator implements ConstraintValidator<GenderConstraint, String> {

    @Override
    public void initialize(GenderConstraint gender) {
    }

    @Override
    public boolean isValid(String gender, ConstraintValidatorContext constraintValidatorContext) {
        if (gender == null) {
            throw new CustomUserException(INVALID_GENDER_NULL, "gender cannot be null");
        }
        if (gender.isBlank()) {
            throw new CustomUserException(INVALID_GENDER_BLANK, "gender cannot be blank");
        }
        if (gender.equals(Gender.M.name()) || gender.equals(Gender.F.name())) {
            return true;
        }

        throw new CustomUserException(INVALID_GENDER, "gender should be 'M' or 'F'");
    }
}
