package org.websoso.WSSServer.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.websoso.WSSServer.user.domain.Block.UNIQUE_CONSTRAINT_NAME;

import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class BlockConstraintViolationDetectorTest {

    private final BlockConstraintViolationDetector detector = new BlockConstraintViolationDetector();

    @DisplayName("차단 유니크 키 위반을 중복 차단으로 판별한다")
    @Test
    void detectsDuplicateBlock() {
        DataIntegrityViolationException exception = dataIntegrityViolation(UNIQUE_CONSTRAINT_NAME);

        assertThat(detector.isDuplicateBlock(exception)).isTrue();
    }

    @DisplayName("다른 무결성 제약조건 위반은 중복 차단으로 판별하지 않는다")
    @Test
    void ignoresUnrelatedConstraintViolation() {
        DataIntegrityViolationException exception = dataIntegrityViolation("uk_unrelated_constraint");

        assertThat(detector.isDuplicateBlock(exception)).isFalse();
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
