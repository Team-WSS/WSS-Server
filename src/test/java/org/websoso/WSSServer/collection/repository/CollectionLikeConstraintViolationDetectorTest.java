package org.websoso.WSSServer.collection.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * 좋아요 등록은 upsert이므로 중복은 예외로 올라오지 않는다. 판별이 필요한 실패는 "참조 대상 컬렉션이
 * 사라짐" 하나이며, 등록이 JDBC로 실행되어 Hibernate 예외가 만들어지지 않으므로 판별은
 * MySQL 오류 코드로 한다. 실제 코드가 어떤 상황에서 나오는지는 DB 없이 확인할 수 없으므로,
 * 여기서는 코드별로 판별이 갈리는지를 확인한다.
 */
class CollectionLikeConstraintViolationDetectorTest {

    private static final int NO_REFERENCED_ROW = 1452;
    private static final int NO_REFERENCED_ROW_LEGACY = 1216;
    private static final int DUPLICATE_ENTRY = 1062;
    private static final int NOT_NULL_VIOLATION = 1048;

    private final CollectionLikeConstraintViolationDetector detector =
            new CollectionLikeConstraintViolationDetector();

    /**
     * `collection_like`의 외래 키는 컬렉션을 가리키는 하나뿐이므로(14.3절), 이 테이블 INSERT에서
     * 참조 대상이 없다는 오류는 좋아요를 넣는 사이에 컬렉션이 삭제된 경우다.
     */
    @DisplayName("참조 대상 행이 없다는 오류 코드를 사라진 컬렉션으로 판별한다")
    @ParameterizedTest
    @ValueSource(ints = {NO_REFERENCED_ROW, NO_REFERENCED_ROW_LEGACY})
    void detectsMissingCollection(int errorCode) {
        assertThat(detector.isMissingCollection(dataIntegrityViolation(errorCode))).isTrue();
    }

    /**
     * 중복 좋아요는 upsert가 흡수하므로 여기까지 오지 않지만, 온다면 그것은 사라진 컬렉션이 아니다.
     * 잘못 판별하면 멱등하게 끝나야 할 요청이 404로 나간다.
     */
    @DisplayName("유니크 위반은 사라진 컬렉션으로 판별하지 않는다")
    @Test
    void ignoresDuplicateEntry() {
        assertThat(detector.isMissingCollection(dataIntegrityViolation(DUPLICATE_ENTRY))).isFalse();
    }

    @DisplayName("다른 무결성 오류 코드는 사라진 컬렉션으로 판별하지 않는다")
    @Test
    void ignoresUnrelatedIntegrityViolation() {
        assertThat(detector.isMissingCollection(dataIntegrityViolation(NOT_NULL_VIOLATION))).isFalse();
    }

    @DisplayName("SQL 예외 원인이 없으면 사라진 컬렉션으로 판별하지 않는다")
    @Test
    void ignoresExceptionWithoutSqlCause() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("data integrity violation");

        assertThat(detector.isMissingCollection(exception)).isFalse();
    }

    /**
     * 드라이버 예외를 감싸는 깊이는 예외 변환기와 드라이버에 따라 다르므로 원인 체인을 끝까지 따라간다.
     */
    @DisplayName("원인 체인 깊은 곳의 SQL 예외도 판별한다")
    @Test
    void detectsNestedSqlException() {
        SQLException sqlException = new SQLIntegrityConstraintViolationException(
                "foreign key constraint fails", "23000", NO_REFERENCED_ROW);
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "data integrity violation", new IllegalStateException(sqlException));

        assertThat(detector.isMissingCollection(exception)).isTrue();
    }

    /**
     * 판별은 오류 메시지 문구가 아니라 코드로 한다. 드라이버·DB 버전마다 달라지는 문구에 기대면
     * 문구가 바뀌는 순간 판별이 조용히 어긋난다(13절).
     */
    @DisplayName("메시지에 외래 키 이름이 없어도 오류 코드만으로 판별한다")
    @Test
    void doesNotDependOnMessageText() {
        SQLException sqlException = new SQLException("", "23000", NO_REFERENCED_ROW);
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("data integrity violation", sqlException);

        assertThat(detector.isMissingCollection(exception)).isTrue();
    }

    private DataIntegrityViolationException dataIntegrityViolation(int errorCode) {
        SQLException cause = new SQLIntegrityConstraintViolationException(
                "constraint violation", "23000", errorCode);

        return new DataIntegrityViolationException("data integrity violation", cause);
    }
}
