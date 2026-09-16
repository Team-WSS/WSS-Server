package org.websoso.WSSServer.collection.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.websoso.WSSServer.collection.domain.CollectionNovel.UNIQUE_CONSTRAINT_NAME;

import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;

class CollectionNovelConstraintViolationDetectorTest {

    private final CollectionNovelConstraintViolationDetector detector =
            new CollectionNovelConstraintViolationDetector();

    @DisplayName("테이블명 포함 여부와 관계없이 컬렉션 작품 유니크 키 위반을 중복 작품으로 판별한다")
    @ParameterizedTest
    @ValueSource(strings = {
            UNIQUE_CONSTRAINT_NAME,
            "collection_novel." + UNIQUE_CONSTRAINT_NAME,
            "UK_COLLECTION_NOVEL_COLLECTION_NOVEL"
    })
    void detectsDuplicateCollectionNovel(String constraintName) {
        DataIntegrityViolationException exception = dataIntegrityViolation(constraintName);

        assertThat(detector.isDuplicateCollectionNovel(exception)).isTrue();
    }

    @DisplayName("다른 무결성 제약조건 위반은 중복 작품으로 판별하지 않는다")
    @Test
    void ignoresUnrelatedConstraintViolation() {
        DataIntegrityViolationException exception = dataIntegrityViolation("uk_block_blocking_blocked");

        assertThat(detector.isDuplicateCollectionNovel(exception)).isFalse();
    }

    @DisplayName("제약조건 이름을 알 수 없으면 중복 작품으로 판별하지 않는다")
    @Test
    void ignoresUnknownConstraintName() {
        DataIntegrityViolationException exception = dataIntegrityViolation(null);

        assertThat(detector.isDuplicateCollectionNovel(exception)).isFalse();
    }

    @DisplayName("제약조건 위반 원인이 없으면 중복 작품으로 판별하지 않는다")
    @Test
    void ignoresExceptionWithoutConstraintViolationCause() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("data integrity violation", new SQLException());

        assertThat(detector.isDuplicateCollectionNovel(exception)).isFalse();
    }

    @DisplayName("원인 체인 깊은 곳의 제약조건 위반도 판별한다")
    @Test
    void detectsNestedConstraintViolation() {
        ConstraintViolationException constraintViolation = new ConstraintViolationException(
                "constraint violation", new SQLException(), UNIQUE_CONSTRAINT_NAME);
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "data integrity violation", new IllegalStateException(constraintViolation));

        assertThat(detector.isDuplicateCollectionNovel(exception)).isTrue();
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
