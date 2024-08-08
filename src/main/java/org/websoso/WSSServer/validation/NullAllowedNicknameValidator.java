package org.websoso.WSSServer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NullAllowedNicknameValidator implements ConstraintValidator<NullAllowedNicknameConstraint, String> {

    private final NicknameValidator nicknameValidator;

    @Override
    public void initialize(NullAllowedNicknameConstraint nickname) {
    }

    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext constraintValidatorContext) {
        if (nickname == null) {
            return true;
        }
        return nicknameValidator.isValid(nickname, constraintValidatorContext);
    }
}
