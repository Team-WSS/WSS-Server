package org.websoso.WSSServer.domain.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.websoso.WSSServer.domain.common.UserNovelSortType.READ_DATE;
import static org.websoso.WSSServer.library.domain.QUserNovel.userNovel;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.OrderSpecifier.NullHandling;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 사용자 서재 정렬 유형의 QueryDSL 정렬 조건을 검증한다.
 */
class UserNovelSortTypeTest {

    // 날짜순 정렬이 상태별 독서 날짜와 날짜 없는 항목의 등록일을 사용하는지 검증한다.
    @Test
    @DisplayName("날짜순 정렬은 상태별 독서 날짜를 기준으로 최신순 정렬한다")
    void orderByReadDate() {
        List<OrderSpecifier<?>> orderSpecifiers = READ_DATE.orderSpecifiers();

        assertThat(orderSpecifiers).hasSize(3);
        assertThat(orderSpecifiers.get(0).getOrder()).isEqualTo(Order.DESC);
        assertThat(orderSpecifiers.get(0).getNullHandling()).isEqualTo(NullHandling.NullsLast);
        assertThat(orderSpecifiers.get(0).getTarget().toString())
                .contains("status", "WATCHING", "startDate", "endDate");
        assertThat(orderSpecifiers.get(1).getOrder()).isEqualTo(Order.DESC);
        assertThat(orderSpecifiers.get(1).getNullHandling()).isEqualTo(NullHandling.NullsLast);
        assertThat(orderSpecifiers.get(1).getTarget().toString())
                .contains("startDate", "endDate", "createdDate");
        assertThat(orderSpecifiers.get(2).getTarget()).isEqualTo(userNovel.userNovelId);
        assertThat(orderSpecifiers.get(2).getOrder()).isEqualTo(Order.DESC);
    }
}
