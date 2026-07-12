package org.websoso.WSSServer.feed.feed.repository;

import static org.websoso.WSSServer.feed.feed.domain.QFeed.feed;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;
import static org.websoso.WSSServer.novel.domain.QNovelStatistics.novelStatistics;
import static org.websoso.WSSServer.user.domain.QAvatarProfile.avatarProfile;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.QGenre;
import org.websoso.WSSServer.domain.common.FeedImageType;

import org.websoso.WSSServer.feed.comment.domain.QComment;
import org.websoso.WSSServer.feed.feed.domain.QFeedImage;
import org.websoso.WSSServer.feed.feed.domain.QLike;
import org.websoso.WSSServer.feed.feed.repository.projection.FeedInfoRow;
import org.websoso.WSSServer.feed.feed.repository.projection.PopularFeedInfoRow;
import org.websoso.WSSServer.feed.feed.repository.projection.UserFeedInfoRow;
import org.websoso.WSSServer.library.domain.QUserNovel;
import org.websoso.WSSServer.novel.domain.QNovelGenre;

@Repository
@RequiredArgsConstructor
public class FeedQueryRepositoryImpl implements FeedQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<FeedInfoRow> findFeedInfoRows(List<Long> feedIds, Long userId) {
        if (feedIds.isEmpty()) {
            return List.of();
        }

        QFeedImage thumbnailImage = new QFeedImage("thumbnailImage");

        return jpaQueryFactory
                .select(Projections.constructor(
                        FeedInfoRow.class,
                        feed.feedId,
                        feed.user.userId,
                        feed.user.nickname,
                        avatarProfile.avatarProfileImage,
                        feed.createdDate,
                        feed.feedContent,
                        likeCount(),
                        isLiked(userId),
                        commentCount(),
                        feed.novelId,
                        novel.title,
                        novelStatistics.ratingCount,
                        novelStatistics.averageRating,
                        feed.isSpoiler,
                        feed.createdDate.ne(feed.modifiedDate),
                        isMyFeed(userId),
                        feed.isPublic,
                        thumbnailImage.url,
                        imageCount(),
                        firstGenreName(),
                        userNovelRating(userId),
                        feedWriterNovelRating()
                ))
                .from(feed)
                .join(feed.user)
                .leftJoin(avatarProfile).on(feed.user.avatarProfileId.eq(avatarProfile.avatarProfileId))
                .leftJoin(novel).on(feed.novelId.eq(novel.novelId))
                .leftJoin(novel.novelStatistics, novelStatistics)
                .leftJoin(thumbnailImage).on(
                        thumbnailImage.feedId.eq(feed.feedId),
                        thumbnailImage.feedImageType.eq(FeedImageType.FEED_THUMBNAIL)
                )
                .where(feed.feedId.in(feedIds))
                .fetch();
    }

    @Override
    public List<UserFeedInfoRow> findUserFeedInfoRows(List<Long> feedIds, Long visitorId) {
        if (feedIds.isEmpty()) {
            return List.of();
        }

        QFeedImage thumbnailImage = new QFeedImage("userFeedThumbnailImage");

        return jpaQueryFactory
                .select(Projections.constructor(
                        UserFeedInfoRow.class,
                        feed.feedId,
                        feed.feedContent,
                        feed.createdDate,
                        feed.isSpoiler,
                        feed.createdDate.ne(feed.modifiedDate),
                        isLiked(visitorId),
                        likeCount(),
                        commentCount(),
                        feed.novelId,
                        novel.title,
                        novelRating(),
                        novelRatingCount(),
                        feed.isPublic,
                        firstGenreName(),
                        userNovelRating(visitorId),
                        thumbnailImage.url,
                        imageCount(),
                        feedWriterNovelRating()
                ))
                .from(feed)
                .join(feed.user)
                .leftJoin(novel).on(feed.novelId.eq(novel.novelId))
                .leftJoin(thumbnailImage).on(
                        thumbnailImage.feedId.eq(feed.feedId),
                        thumbnailImage.feedImageType.eq(FeedImageType.FEED_THUMBNAIL)
                )
                .where(feed.feedId.in(feedIds))
                .fetch();
    }

    @Override
    public List<PopularFeedInfoRow> findPopularFeedInfoRowsByFeedIds(List<Long> feedIds) {
        if (feedIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        PopularFeedInfoRow.class,
                        feed.feedId,
                        feed.feedContent,
                        likeCount(),
                        commentCount(),
                        feed.isSpoiler,
                        feed.isPublic,
                        novel.title,
                        novel.novelImage,
                        firstGenreName()
                ))
                .from(feed)
                .leftJoin(novel).on(feed.novelId.eq(novel.novelId))
                .where(feed.feedId.in(feedIds))
                .fetch();
    }

    private JPQLQuery<Long> likeCount() {
        QLike likeSub = new QLike("likeCountSub");

        return JPAExpressions
                .select(likeSub.likeId.count())
                .from(likeSub)
                .where(likeSub.feed.eq(feed));
    }

    private JPQLQuery<Long> commentCount() {
        QComment commentSub = new QComment("commentCountSub");

        return JPAExpressions
                .select(commentSub.commentId.count())
                .from(commentSub)
                .where(commentSub.feed.eq(feed));
    }

    private JPQLQuery<Long> imageCount() {
        QFeedImage imageSub = new QFeedImage("imageCountSub");

        return JPAExpressions
                .select(imageSub.feedImageId.count())
                .from(imageSub)
                .where(imageSub.feedId.eq(feed.feedId));
    }

    private Expression<Boolean> isLiked(Long userId) {
        if (userId == null) {
            return Expressions.FALSE;
        }

        QLike likeSub = new QLike("likedSub");

        return JPAExpressions
                .selectOne()
                .from(likeSub)
                .where(
                        likeSub.userId.eq(userId),
                        likeSub.feed.eq(feed)
                )
                .exists();
    }

    private Expression<Boolean> isMyFeed(Long userId) {
        if (userId == null) {
            return Expressions.FALSE;
        }

        return feed.user.userId.eq(userId);
    }

    private JPQLQuery<Long> novelRatingCount() {
        QUserNovel ratingSub = new QUserNovel("ratingCountSub");

        return JPAExpressions
                .select(ratingSub.userNovelId.count())
                .from(ratingSub)
                .where(
                        ratingSub.novel.eq(novel),
                        ratingSub.userNovelRating.gt(0.0f)
                );
    }

    private JPQLQuery<Double> novelRating() {
        QUserNovel ratingSub = new QUserNovel("ratingAvgSub");

        return JPAExpressions
                .select(ratingSub.userNovelRating.avg())
                .from(ratingSub)
                .where(
                        ratingSub.novel.eq(novel),
                        ratingSub.userNovelRating.gt(0.0f)
                );
    }

    private JPQLQuery<String> firstGenreName() {
        QNovelGenre novelGenreSub = new QNovelGenre("firstGenreSub");
        QGenre genreSub = new QGenre("genreSub");

        return JPAExpressions
                .select(genreSub.genreName.min())
                .from(novelGenreSub)
                .join(novelGenreSub.genre, genreSub)
                .where(novelGenreSub.novel.eq(novel));
    }

    private Expression<Float> userNovelRating(Long userId) {
        if (userId == null) {
            return Expressions.nullExpression(Float.class);
        }

        QUserNovel userNovelSub = new QUserNovel("userNovelRatingSub");

        return JPAExpressions
                .select(userNovelSub.userNovelRating)
                .from(userNovelSub)
                .where(
                        userNovelSub.novel.eq(novel),
                        userNovelSub.user.userId.eq(userId)
                );
    }

    private JPQLQuery<Float> feedWriterNovelRating() {
        QUserNovel writerNovelSub = new QUserNovel("writerNovelRatingSub");

        return JPAExpressions
                .select(writerNovelSub.userNovelRating)
                .from(writerNovelSub)
                .where(
                        writerNovelSub.novel.eq(novel),
                        writerNovelSub.user.userId.eq(feed.user.userId),
                        writerNovelSub.status.isNotNull()
                );
    }

}
