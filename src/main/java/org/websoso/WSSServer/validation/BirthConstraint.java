package org.websoso.WSSServer.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = BirthValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface BirthConstraint {
    String message() default "출생연도는 1900 ~ 2024 사이여야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
