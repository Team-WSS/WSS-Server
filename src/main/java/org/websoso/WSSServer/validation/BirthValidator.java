package org.websoso.WSSServer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Year;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class BirthValidator implements ConstraintValidator<BirthConstraint, Integer> {

    private static final int MIN_YEAR = 1900;

    @Override
    public void initialize(BirthConstraint year) {
    }

    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext constraintValidatorContext) {
        if (year == null) {
            return true;
        }

        int currentYear = Year.now().getValue();
        return year >= MIN_YEAR && year <= currentYear;
    }
}
