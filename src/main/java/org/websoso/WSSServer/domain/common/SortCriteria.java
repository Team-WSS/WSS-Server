package org.websoso.WSSServer.domain.common;

import static org.websoso.WSSServer.exception.error.CustomSortCriteriaError.SORT_CRITERIA_NOT_FOUND;

import org.websoso.WSSServer.exception.exception.CustomSortCriteriaException;

public enum SortCriteria {
    RECENT,
    OLD;

    public static SortCriteria create(final String sortCriteria) {
        for (SortCriteria value : SortCriteria.values()) {
            if (value.toString().equals(sortCriteria)) {
                return value;
            }
        }
        throw new CustomSortCriteriaException(SORT_CRITERIA_NOT_FOUND,
                "given sort criteria does not exist");
    }
}
