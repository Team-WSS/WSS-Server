package org.websoso.WSSServer.validation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = BlockIdValidator.class)
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface BlockIdConstraint {
    String message() default "invalid blockId.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
