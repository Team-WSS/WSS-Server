package org.websoso.WSSServer.feed.repository;

import static org.websoso.WSSServer.domain.QBlock.block;
import static org.websoso.WSSServer.domain.QGenre.genre;
import static org.websoso.WSSServer.feed.domain.QFeed.feed;
import static org.websoso.WSSServer.feed.domain.QFeedCategory.feedCategory;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;
import static org.websoso.WSSServer.novel.domain.QNovelGenre.novelGenre;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.domain.Category;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.domain.Genre;

@Repository
@RequiredArgsConstructor
public class FeedCategoryCustomRepositoryImpl implements FeedCategoryCustomRepository {

    private static final long NO_CURSOR = 0L;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Feed> findRecommendedFeedsByCategoryLabel(Category category, Long lastFeedId, Long userId,
                                                           PageRequest pageRequest, List<Genre> genres) {

        List<Feed> results = jpaQueryFactory
                .select(feedCategory.feed)
                .from(feedCategory)
                .leftJoin(feedCategory.feed, feed)
                .leftJoin(novel).on(feed.novelId.eq(novel.novelId))
                .leftJoin(novelGenre).on(novel.eq(novelGenre.novel))
                .leftJoin(genre).on(novelGenre.genre.eq(genre))
                .where(
                        ltFeedId(lastFeedId),
                        checkCategory(category),
                        checkHidden(),
                        checkBlocking(userId),
                        checkGenres(genres)
                )
                .orderBy(feed.feedId.desc())
                .limit(pageRequest.getPageSize() + 1)
                .fetch();

        boolean hasNext = results.size() > pageRequest.getPageSize();
        if (hasNext) {
            results.remove(results.size() - 1);
        }

        return new SliceImpl<>(results, pageRequest, hasNext);
    }

    private BooleanExpression ltFeedId(Long lastFeedId) {
        if (lastFeedId == NO_CURSOR) {
            return null;
        }
        return feed.feedId.lt(lastFeedId);
    }

    private BooleanExpression checkCategory(Category category) {
        return feedCategory.category.eq(category);
    }

    private BooleanExpression checkHidden() {
        return feed.isHidden.eq(false);
    }

    private BooleanExpression checkBlocking(Long userId) {
        if (userId != null) {
            return feed.user.userId.notIn(
                    JPAExpressions
                            .select(block.blockedId)
                            .from(block)
                            .where(block.blockingId.eq(userId)) // userId는 파라미터
            );
        }
        return null;
    }

    private BooleanExpression checkGenres(List<Genre> genres) {
        if (genres != null && !genres.isEmpty()) {
            return genre.in(genres).or(feed.novelId.isNull());
        }
        return null;
    }
}
