package org.websoso.WSSServer.feed.report.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.websoso.WSSServer.feed.report.domain.ReportedFeed.UNIQUE_CONSTRAINT_NAME;

import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.websoso.WSSServer.feed.report.domain.ReportedComment;

class ReportConstraintViolationDetectorTest {

    private final ReportConstraintViolationDetector detector = new ReportConstraintViolationDetector();

    @DisplayName("테이블명 포함 여부와 관계없이 피드 신고 유니크 키 위반을 중복 신고로 판별한다")
    @ParameterizedTest
    @ValueSource(strings = {
            UNIQUE_CONSTRAINT_NAME,
            "reported_feed." + UNIQUE_CONSTRAINT_NAME
    })
    void detectsDuplicateFeedReport(String constraintName) {
        DataIntegrityViolationException exception = dataIntegrityViolation(constraintName);

        assertThat(detector.isDuplicateFeedReport(exception)).isTrue();
    }

    @DisplayName("테이블명 포함 여부와 관계없이 댓글 신고 유니크 키 위반을 중복 신고로 판별한다")
    @ParameterizedTest
    @ValueSource(strings = {
            ReportedComment.UNIQUE_CONSTRAINT_NAME,
            "reported_comment." + ReportedComment.UNIQUE_CONSTRAINT_NAME
    })
    void detectsDuplicateCommentReport(String constraintName) {
        DataIntegrityViolationException exception = dataIntegrityViolation(constraintName);

        assertThat(detector.isDuplicateCommentReport(exception)).isTrue();
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
