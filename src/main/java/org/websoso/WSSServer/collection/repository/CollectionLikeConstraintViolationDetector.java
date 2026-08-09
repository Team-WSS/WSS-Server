package org.websoso.WSSServer.collection.repository;

import java.sql.SQLException;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * 컬렉션 좋아요 등록이 실패한 이유가 "참조 대상 컬렉션이 사라짐"인지 판별한다.
 * <p>
 * 중복 좋아요는 여기서 다루지 않는다. 등록이 upsert이므로 유니크 제약조건 위반은 예외가 아니라
 * DB가 흡수하는 정상 종료다(16.2절). 판별이 필요한 실패는 외래 키 위반 하나뿐이다.
 * <p>
 * 판별은 오류 메시지 전문이 아니라 MySQL이 돌려주는 <b>오류 코드</b>로 한다. 등록이 JDBC로 실행되어
 * Hibernate의 {@code ConstraintViolationException}이 만들어지지 않고, MySQL 드라이버는 위반한
 * 제약조건 이름을 메시지 문자열 안에만 담기 때문이다. 드라이버·DB 버전마다 달라지는 문구를 파싱하면
 * 문구가 바뀌는 순간 판별이 조용히 어긋나지만, 오류 코드는 그 문구와 독립적이다(13절).
 * <p>
 * 판별하지 못한 데이터 무결성 오류는 호출하는 쪽이 그대로 던져 숨기지 않는다.
 */
@Component
public class CollectionLikeConstraintViolationDetector {

    /**
     * 참조 대상 행이 없어 자식 행을 넣거나 고칠 수 없을 때 MySQL이 돌려주는 오류 코드.
     * {@code 1216}은 {@code ER_NO_REFERENCED_ROW}, {@code 1452}는 {@code ER_NO_REFERENCED_ROW_2}다.
     */
    private static final Set<Integer> NO_REFERENCED_ROW_ERROR_CODES = Set.of(1216, 1452);

    /**
     * 좋아요를 등록하는 사이에 컬렉션이 삭제되어 참조 대상 행이 사라졌는지 판별한다.
     * <p>
     * {@code collection_like}의 외래 키는 컬렉션을 가리키는 {@code fk_collection_like_collection}
     * 하나뿐이므로(14.3절, {@code user_id}에는 외래 키를 걸지 않는다), 이 테이블에 대한 INSERT에서
     * 참조 대상이 없다는 오류는 사라진 컬렉션 외에 다른 경우가 없다.
     * <p>
     * 유니크 위반({@code 1062}), NOT NULL 위반({@code 1048}) 같은 다른 무결성 오류는 코드가 다르므로
     * 여기서 참으로 판별되지 않는다.
     */
    public boolean isMissingCollection(DataIntegrityViolationException exception) {
        SQLException sqlException = findSqlException(exception);

        return sqlException != null && NO_REFERENCED_ROW_ERROR_CODES.contains(sqlException.getErrorCode());
    }

    /**
     * 원인 체인에서 처음 만나는 SQL 예외를 돌려준다. Spring이 던지는 예외는 드라이버 예외를 감싸고 있고,
     * 감싸는 깊이는 예외 변환기와 드라이버에 따라 다르다. SQL 예외가 없으면 {@code null}이다.
     */
    private SQLException findSqlException(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof SQLException sqlException) {
                return sqlException;
            }
            cause = cause.getCause();
        }

        return null;
    }
}
