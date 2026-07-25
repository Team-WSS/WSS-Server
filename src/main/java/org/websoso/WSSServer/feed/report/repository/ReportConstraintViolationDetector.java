package org.websoso.WSSServer.feed.report.repository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.feed.report.domain.ReportedComment;
import org.websoso.WSSServer.feed.report.domain.ReportedFeed;

@Component
public class ReportConstraintViolationDetector {

    public boolean isDuplicateFeedReport(DataIntegrityViolationException exception) {
        return isConstraintViolation(exception, ReportedFeed.UNIQUE_CONSTRAINT_NAME);
    }

    public boolean isDuplicateCommentReport(DataIntegrityViolationException exception) {
        return isConstraintViolation(exception, ReportedComment.UNIQUE_CONSTRAINT_NAME);
    }

    private boolean isConstraintViolation(DataIntegrityViolationException exception, String constraintName) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                String detectedConstraintName = normalizeConstraintName(
                        constraintViolationException.getConstraintName()
                );
                return constraintName.equalsIgnoreCase(detectedConstraintName);
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
