package org.websoso.WSSServer.feed.report.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.websoso.WSSServer.feed.report.domain.ReportedFeed.UNIQUE_CONSTRAINT_NAME;

import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class ReportConstraintViolationDetectorTest {

    private final ReportConstraintViolationDetector detector = new ReportConstraintViolationDetector();

    @DisplayName("피드 신고 유니크 키 위반을 중복 신고로 판별한다")
    @Test
    void detectsDuplicateFeedReport() {
        DataIntegrityViolationException exception = dataIntegrityViolation(UNIQUE_CONSTRAINT_NAME);

        assertThat(detector.isDuplicateFeedReport(exception)).isTrue();
    }

    @DisplayName("다른 무결성 제약조건 위반은 중복 신고로 판별하지 않는다")
    @Test
    void ignoresUnrelatedConstraintViolation() {
        DataIntegrityViolationException exception = dataIntegrityViolation("uk_unrelated_constraint");

        assertThat(detector.isDuplicateFeedReport(exception)).isFalse();
    }

    private DataIntegrityViolationException dataIntegrityViolation(String constraintName) {
        ConstraintViolationException cause = new ConstraintViolationException(
                "constraint violation",
                new SQLException(),
                constraintName
        );
        return new DataIntegrityViolationException("data integrity violation", cause);
    }
}
