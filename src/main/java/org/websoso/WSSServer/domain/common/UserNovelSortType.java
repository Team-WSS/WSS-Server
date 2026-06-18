package org.websoso.WSSServer.domain.common;

import static org.websoso.WSSServer.exception.error.CustomFilteringError.SORT_CRITERIA_NOT_FOUND;
import static org.websoso.WSSServer.library.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import java.util.List;
import org.websoso.WSSServer.exception.exception.CustomFilteringException;

public enum UserNovelSortType {
    CREATED_DESC("created_desc"),
    CREATED_ASC("created_asc"),
    TITLE("title"),
    RATING_DESC("rating_desc"),
    RATING_ASC("rating_asc");

    private final String value;

    UserNovelSortType(String value) {
        this.value = value;
    }

    public static UserNovelSortType of(String sortType) {
        if (sortType == null || sortType.isBlank()) {
            return CREATED_DESC;
        }

        for (UserNovelSortType value : UserNovelSortType.values()) {
            if (value.value.equalsIgnoreCase(sortType)) {
                return value;
            }
        }

        throw new CustomFilteringException(SORT_CRITERIA_NOT_FOUND,
                "given user novel sort type does not exist");
    }

    public List<OrderSpecifier<?>> orderSpecifiers() {
        return switch (this) {
            case CREATED_DESC -> List.of(userNovel.createdDate.desc(), userNovel.userNovelId.desc());
            case CREATED_ASC -> List.of(userNovel.createdDate.asc(), userNovel.userNovelId.asc());
            case TITLE -> List.of(novel.title.asc(), userNovel.userNovelId.asc());
            case RATING_DESC -> List.of(
                    new CaseBuilder()
                            .when(userNovel.userNovelRating.eq(0.0f))
                            .then(1)
                            .otherwise(0)
                            .asc(),
                    userNovel.userNovelRating.desc(),
                    userNovel.createdDate.desc(),
                    userNovel.userNovelId.desc()
            );
            case RATING_ASC -> List.of(
                    new CaseBuilder()
                            .when(userNovel.userNovelRating.eq(0.0f))
                            .then(0)
                            .otherwise(1)
                            .asc(),
                    userNovel.userNovelRating.asc(),
                    userNovel.createdDate.desc(),
                    userNovel.userNovelId.desc()
            );
        };
    }
}
