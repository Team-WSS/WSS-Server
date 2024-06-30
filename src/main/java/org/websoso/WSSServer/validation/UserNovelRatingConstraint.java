package org.websoso.WSSServer.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Constraint(validatedBy = UserNovelRatingValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface UserNovelRatingConstraint {

    String message() default "Rating must be between 0.0, 5.0 and a multiple of 0.5.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
