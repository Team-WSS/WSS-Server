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
                String constraintName = normalizeConstraintName(
                        constraintViolationException.getConstraintName()
                );
                return Block.UNIQUE_CONSTRAINT_NAME.equalsIgnoreCase(
                        constraintName
                );
            }
            cause = cause.getCause();
        }

        return false;
    }

    private String normalizeConstraintName(String constraintName) {
        if (constraintName == null) {
            return null;
        }

        int tableNameSeparatorIndex = constraintName.lastIndexOf('.');
        if (tableNameSeparatorIndex < 0) {
            return constraintName;
        }

        return constraintName.substring(tableNameSeparatorIndex + 1);
    }
}
