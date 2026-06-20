package org.websoso.WSSServer.feed.feed.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.feed.controller.dto.FeedCreateRequest;
import org.websoso.WSSServer.feed.feed.controller.dto.FeedCreateResponse;
import org.websoso.WSSServer.feed.feed.controller.dto.FeedImageCreateRequest;
import org.websoso.WSSServer.feed.feed.event.FeedImageDeleteEvent;
import org.websoso.WSSServer.feed.feed.controller.dto.FeedImageUpdateRequest;
import org.websoso.WSSServer.feed.feed.controller.dto.FeedUpdateRequest;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.feed.domain.FeedImage;
import org.websoso.WSSServer.feed.comment.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.feed.service.FeedImageService;
import org.websoso.WSSServer.feed.feed.service.FeedLikeService;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.user.domain.User;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedManagementApplication {

    private final FeedServiceImpl feedService;
    private final FeedLikeService feedLikeService;
    private final CommentServiceImpl commentService;
    private final NovelServiceImpl novelService;
    private final FeedImageService feedImageService;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public FeedCreateResponse create(User user, FeedCreateRequest request, FeedImageCreateRequest imagesRequest) {

        // 입력한 소설이 존재하는지만 체크 (트랜잭션을 여기서는 잠글 필요가 없음?)
        novelService.validateNovelExistsIfPresent(request.novelId());

        // 이미지 업로드
        List<FeedImage> feedImages = feedImageService.processFeedImages(imagesRequest.images());

        // 피드 객체 생성
        Feed feed = Feed.create(request.feedContent(), request.novelId(), request.isSpoiler(), request.isPublic(), user, feedImages);

        // 피드 저장
        feedService.createFeed(feed);

        // 반환
        return FeedCreateResponse.of(feedImages);
    }

    @Transactional
    public FeedCreateResponse update(User user, Long feedId, FeedUpdateRequest request, FeedImageUpdateRequest imagesRequest) {

        // 사용자가 작성한 피드인지 확인
        Feed feed = feedService.getOwnedFeedOrException(feedId, user.getUserId());

        // 기존 이미지를 임시 저장
        List<FeedImage> oldImages = new ArrayList<>(feed.getImages());

        // 소설이 변경된 경우 존재하는 소설인지 체크
        if (feed.isNovelChanged(request.novelId())) {
            novelService.validateNovelExistsIfPresent(request.novelId());
        }

        // 이미지 업로드
        List<FeedImage> feedImages = feedImageService.processFeedImages(imagesRequest.images());

        // 피드 업데이트
        feed.updateFeed(request.feedContent(), request.isSpoiler(), request.isPublic(), request.novelId(), feedImages);

        // 과거 이미지를 String 리스트로 변환 및 이벤트 리스너를 통해 커밋시 삭제
        List<String> oldImageUrls = oldImages.stream().map(FeedImage::getUrl).toList();
        eventPublisher.publishEvent(new FeedImageDeleteEvent(oldImageUrls));

        return FeedCreateResponse.of(feedImages);
    }

    @Transactional
    public void delete(User user, Long feedId) {

        // 사용자가 작성한 피드인지 확인
        Feed feed = feedService.getOwnedFeedOrException(feedId, user.getUserId());

        // 댓글 삭제 (댓글 / 신고 내역)
        commentService.deleteByFeedId(feed.getFeedId());

        // 좋아요 내역 삭제
        feedLikeService.deleteByFeedId(feed.getFeedId());

        // 피드 삭제
        feedService.delete(feed);
    }

    @Transactional
    public void updateFeedWriterToUnknown(Long userId) {
        feedService.updateWriterToUnknown(userId);
    }

}
