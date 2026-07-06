package org.websoso.WSSServer.domain.common;

import static org.websoso.WSSServer.exception.error.CustomFilteringError.SORT_CRITERIA_NOT_FOUND;
import static org.websoso.WSSServer.library.domain.QUserNovel.userNovel;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.websoso.WSSServer.exception.exception.CustomFilteringException;

public enum UserNovelSortType {
    CREATED_DESC("created_desc"),
    CREATED_ASC("created_asc"),
    TITLE("title"),
    TITLE_ASC("title_asc"),
    TITLE_DESC("title_desc"),
    READ_DATE("read_date"),
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

    // 정렬 유형에 맞는 정렬 조건과 동일 값 정렬 기준을 반환한다.
    public List<OrderSpecifier<?>> orderSpecifiers() {
        return switch (this) {
            case CREATED_DESC -> List.of(userNovel.createdDate.desc(), userNovel.userNovelId.desc());
            case CREATED_ASC -> List.of(userNovel.createdDate.asc(), userNovel.userNovelId.asc());
            case TITLE, TITLE_ASC -> List.of(novel.title.asc(), userNovel.userNovelId.asc());
            case TITLE_DESC -> List.of(novel.title.desc(), userNovel.userNovelId.desc());
            case READ_DATE -> List.of(
                    readDateExpression().desc().nullsLast(),
                    undatedCreatedDateExpression().desc().nullsLast(),
                    userNovel.userNovelId.desc()
            );
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

    // 독서 상태에 따라 날짜순 정렬에 사용할 날짜 표현식을 생성한다.
    public static DateExpression<LocalDate> readDateExpression() {
        return new CaseBuilder()
                .when(userNovel.status.eq(ReadStatus.WATCHING))
                .then(userNovel.startDate)
                .otherwise(userNovel.endDate);
    }

    // 독서 날짜가 없는 항목에만 등록일 정렬 값을 부여한다.
    private static DateTimeExpression<LocalDateTime> undatedCreatedDateExpression() {
        return new CaseBuilder()
                .when(readDateExpression().isNull())
                .then(userNovel.createdDate)
                .otherwise(Expressions.nullExpression(LocalDateTime.class));
    }
}
