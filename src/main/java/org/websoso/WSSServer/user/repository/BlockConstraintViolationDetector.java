package org.websoso.WSSServer.user.repository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.user.domain.Block;

@Component
public class BlockConstraintViolationDetector {

    public boolean isDuplicateBlock(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                return Block.UNIQUE_CONSTRAINT_NAME.equalsIgnoreCase(
                        constraintViolationException.getConstraintName()
                );
            }
            cause = cause.getCause();
        }

        return false;
    }
}
