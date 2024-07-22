package org.websoso.WSSServer.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = GenderValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface GenderConstraint {
    String message() default "Invalid gender. It should be either 'M' or 'F'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
