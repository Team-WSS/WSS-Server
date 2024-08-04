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
    String message() default "유효하지 않은 출생년도입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
