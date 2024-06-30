package org.websoso.WSSServer.validation;

import static org.websoso.WSSServer.exception.block.BlockErrorCode.INVALID_BLOCK_ID;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class BlockIdValidator implements ConstraintValidator<BlockIdConstraint, Long> {

    @Override
    public void initialize(BlockIdConstraint blockId) {
    }

    @Override
    public boolean isValid(Long blockId, ConstraintValidatorContext constraintValidatorContext) {
        if (blockId <= 0) {
            throw new InvalidBlockIdException(INVALID_BLOCK_ID, "given blockId is an invalid value");
        }

        return true;
    }
}
