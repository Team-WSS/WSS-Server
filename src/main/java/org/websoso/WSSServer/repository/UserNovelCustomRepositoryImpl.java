package org.websoso.WSSServer.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.QUserNovel;
import org.websoso.WSSServer.domain.common.ReadStatus;

@Repository
@RequiredArgsConstructor
public class UserNovelCustomRepositoryImpl implements UserNovelCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Long> findTodayPopularNovelsId(Pageable pageable) {
        QUserNovel userNovel = QUserNovel.userNovel;
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        return jpaQueryFactory
                .select(userNovel.novel.novelId)
                .from(userNovel)
                .where(userNovel.status.eq(ReadStatus.WATCHING)
                        .or(userNovel.status.eq(ReadStatus.WATCHED))
                        .or(userNovel.isInterest.isTrue())
                        .and(userNovel.createdDate.after(sevenDaysAgo.atStartOfDay())))
                .groupBy(userNovel.novel.novelId)
                .orderBy(userNovel.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
