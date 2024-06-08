package org.websoso.WSSServer.validation;

import static org.websoso.WSSServer.exception.block.BlockErrorCode.BLOCK_NOT_FOUND;
import static org.websoso.WSSServer.exception.block.BlockErrorCode.INVALID_BLOCK_ID;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.exception.block.exception.BlockNotFoundException;
import org.websoso.WSSServer.exception.block.exception.InvalidBlockIdException;
import org.websoso.WSSServer.repository.BlockRepository;

@Component
@AllArgsConstructor
public class BlockIdValidator implements ConstraintValidator<BlockIdConstraint, Long> {

    private final BlockRepository blockRepository;

    @Override
    public void initialize(BlockIdConstraint blockId) {
    }

    @Override
    public boolean isValid(Long blockId, ConstraintValidatorContext constraintValidatorContext) {
        if (blockId <= 0) {
            throw new InvalidBlockIdException(INVALID_BLOCK_ID, "given blockId is an invalid value");
        }
        blockRepository.findById(blockId).orElseThrow(() ->
                new BlockNotFoundException(BLOCK_NOT_FOUND, "block with the given blockId was not found"));

        return true;
    }
}
