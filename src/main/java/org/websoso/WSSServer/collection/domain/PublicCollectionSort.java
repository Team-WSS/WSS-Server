package org.websoso.WSSServer.collection.domain;

import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_SORT;

import org.websoso.WSSServer.collection.exception.CustomCollectionException;

/**
 * 전체 공개 컬렉션 목록의 정렬 기준.
 * <p>
 * 기본값은 최초 생성 시점 최신순이며 커서로 이어 읽는다. {@code random}은 홈의 컬렉션 섹션처럼 첫 페이지만
 * 보여 주는 화면을 위한 단일 페이지 조회로, 커서와 다음 페이지가 없다.
 */
public enum PublicCollectionSort {
    RECENT,
    RANDOM;

    private static final String RANDOM_VALUE = "random";

    /**
     * 생략하거나 빈 값·공백만 있는 값은 기존 최신순으로 본다. 지원하지 않는 값은 요청 오류로 처리한다.
     */
    public static PublicCollectionSort from(String value) {
        if (value == null || value.isBlank()) {
            return RECENT;
        }
        if (RANDOM_VALUE.equals(value)) {
            return RANDOM;
        }

        throw new CustomCollectionException(
                INVALID_COLLECTION_SORT,
                "public collection sort must be omitted or '" + RANDOM_VALUE + "'"
        );
    }

    public boolean isRandom() {
        return this == RANDOM;
    }
}
