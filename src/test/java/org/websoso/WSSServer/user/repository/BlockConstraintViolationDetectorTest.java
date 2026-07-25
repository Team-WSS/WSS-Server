package org.websoso.WSSServer.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.websoso.WSSServer.user.domain.Block.UNIQUE_CONSTRAINT_NAME;

import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;

class BlockConstraintViolationDetectorTest {

    private final BlockConstraintViolationDetector detector = new BlockConstraintViolationDetector();

    @DisplayName("테이블명 포함 여부와 관계없이 차단 유니크 키 위반을 중복 차단으로 판별한다")
    @ParameterizedTest
    @ValueSource(strings = {
            UNIQUE_CONSTRAINT_NAME,
            "block." + UNIQUE_CONSTRAINT_NAME
    })
    void detectsDuplicateBlock(String constraintName) {
        DataIntegrityViolationException exception = dataIntegrityViolation(constraintName);

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
