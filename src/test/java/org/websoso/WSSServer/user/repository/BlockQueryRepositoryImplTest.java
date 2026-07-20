package org.websoso.WSSServer.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.websoso.WSSServer.user.domain.QBlock.block;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.websoso.WSSServer.user.repository.projection.BlockInfoRow;

class BlockQueryRepositoryImplTest {

    private final JPAQueryFactory jpaQueryFactory = mock(JPAQueryFactory.class);
    private final BlockQueryRepositoryImpl repository = new BlockQueryRepositoryImpl(jpaQueryFactory);

    @DisplayName("차단 목록을 blockId 내림차순으로 조회한다")
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void findsBlocksInRecentOrder() {
        JPAQuery<BlockInfoRow> query = mock(JPAQuery.class, RETURNS_SELF);
        given(jpaQueryFactory.select(any(ConstructorExpression.class))).willReturn(query);
        given(query.fetch()).willReturn(List.of());

        repository.findBlockInfoRows(1L);

        ArgumentCaptor<OrderSpecifier> orderCaptor = ArgumentCaptor.forClass(OrderSpecifier.class);
        then(query).should().orderBy(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getOrder()).isEqualTo(Order.DESC);
        assertThat(orderCaptor.getValue().getTarget()).isEqualTo(block.blockId);
    }
}
