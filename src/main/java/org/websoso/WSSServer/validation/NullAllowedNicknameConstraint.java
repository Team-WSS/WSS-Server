package org.websoso.WSSServer.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = NullAllowedNicknameValidator.class)
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface NullAllowedNicknameConstraint {

    String message() default "invalid nickname";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
