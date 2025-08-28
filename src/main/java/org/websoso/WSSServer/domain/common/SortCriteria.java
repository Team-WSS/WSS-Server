package org.websoso.WSSServer.domain.common;

import static org.websoso.WSSServer.exception.error.CustomFilteringError.SORT_CRITERIA_NOT_FOUND;

import org.websoso.WSSServer.exception.exception.CustomFilteringException;

public enum SortCriteria {
    RECENT,
    OLD;

    public static SortCriteria of(final String sortCriteria) {
        for (SortCriteria value : SortCriteria.values()) {
            if (value.toString().equals(sortCriteria)) {
                return value;
            }
        }
        throw new CustomFilteringException(SORT_CRITERIA_NOT_FOUND,
                "given sort criteria does not exist");
    }

    public boolean isOld() {
        return this == OLD;
    }
}
