package org.websoso.WSSServer.collection.repository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.collection.domain.CollectionNovel;

/**
 * 컬렉션 작품 유니크 제약조건 위반만 컬렉션 도메인 예외로 바꿀 수 있도록 판별한다.
 * <p>
 * 지정한 제약조건 위반만 판별하고 그 외 데이터 무결성 오류는 숨기지 않는다.
 */
@Component
public class CollectionNovelConstraintViolationDetector {

    public boolean isDuplicateCollectionNovel(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                String constraintName = normalizeConstraintName(
                        constraintViolationException.getConstraintName()
                );
                return CollectionNovel.UNIQUE_CONSTRAINT_NAME.equalsIgnoreCase(constraintName);
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
