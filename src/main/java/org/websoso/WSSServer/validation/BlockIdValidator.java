package org.websoso.WSSServer.validation;

import static org.websoso.WSSServer.exception.error.CustomBlockError.INVALID_BLOCK_ID;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.exception.exception.CustomBlockException;

@Component
@AllArgsConstructor
public class BlockIdValidator implements ConstraintValidator<BlockIdConstraint, Long> {

    @Override
    public void initialize(BlockIdConstraint blockId) {
    }

    @Override
    public boolean isValid(Long blockId, ConstraintValidatorContext constraintValidatorContext) {
        if (blockId <= 0) {
            throw new CustomBlockException(INVALID_BLOCK_ID, "given blockId is an invalid value");
        }

        return true;
    }
}
