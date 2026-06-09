package org.websoso.WSSServer.feed.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.websoso.WSSServer.dto.feed.*;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.FeedImage;
import org.websoso.WSSServer.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.service.ImageClient;
import org.websoso.WSSServer.user.domain.User;

import java.util.ArrayList;
import java.util.List;

import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_AUTHORIZED;

@Service
@RequiredArgsConstructor
public class FeedManagementApplication {

    private final FeedServiceImpl feedService;
    private final NovelServiceImpl novelService;
    private final ImageClient imageUploader;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public FeedCreateResponse create(User user, FeedCreateRequest request, FeedImageCreateRequest imagesRequest) {

        // 입력한 소설이 존재하는지만 체크 (트랜잭션을 여기서는 잠글 필요가 없음?)
        if (request.novelId() != null) {
            novelService.getNovelOrException(request.novelId());
        }

        // 이미지 업로드
        List<FeedImage> feedImages = processFeedImages(imagesRequest.images());

        // 피드 객체 생성
        Feed feed = Feed.create(request.feedContent(), request.novelId(), request.isSpoiler(), request.isPublic(), user, feedImages);

        // 피드 저장
        feedService.createFeed(feed);

        // 반환
        return FeedCreateResponse.of(feedImages);
    }

    @Transactional
    public FeedCreateResponse update(User user, Long feedId, FeedUpdateRequest request, FeedImageUpdateRequest imagesRequest) {

        // 존재하는 피드인지 확인
        Feed feed = feedService.getFeedOrException(feedId);

        // 본인이 작성한 피드인지 확인
        if (!feed.isMine(user.getUserId())) {
            throw new CustomUserException(INVALID_AUTHORIZED,
                    "User with ID " + user.getUserId() + " is not the owner of feed " + feed.getFeedId());
        }

        // 기존 이미지를 임시 저장
        List<FeedImage> oldImages = new ArrayList<>(feed.getImages());

        // 소설이 변경된 경우 존재하는 소설인지 체크
        if (request.novelId() != null && feed.isNovelChanged(request.novelId())) {
            novelService.getNovelOrException(request.novelId());
        }

        // 이미지 업로드
        List<FeedImage> feedImages = processFeedImages(imagesRequest.images());

        // 피드 업데이트
        feed.updateFeed(request.feedContent(), request.isSpoiler(), request.isPublic(), request.novelId(), feedImages);

        // 과거 이미지를 String 리스트로 변환 및 이벤트 리스너를 통해 커밋시 삭제
        List<String> oldImageUrls = oldImages.stream().map(FeedImage::getUrl).toList();
        eventPublisher.publishEvent(new FeedImageDeleteEvent(oldImageUrls));

        return FeedCreateResponse.of(feedImages);
    }

    // TODO: 이미지 업로드 로직이 여기에서 관리되지 않도록 수정 예정
    private List<FeedImage> processFeedImages(List<MultipartFile> images) {
        List<String> uploadedImageUrls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            try {
                for (MultipartFile image : images) {
                    String imageUrl = imageUploader.uploadFeedImage(image);
                    uploadedImageUrls.add(imageUrl);
                }
            } catch (Exception e) {
                if (!uploadedImageUrls.isEmpty()) {
                    imageUploader.deleteImages(uploadedImageUrls);
                }

                throw e;
            }
        }

        List<FeedImage> feedImages = new ArrayList<>();
        if (!uploadedImageUrls.isEmpty()) {
            feedImages.add(FeedImage.createThumbnail(uploadedImageUrls.get(0)));
            for (int i = 1; i < uploadedImageUrls.size(); i++) {
                feedImages.add(FeedImage.createCommon(uploadedImageUrls.get(i), i));
            }
        }

        return feedImages;
    }

}
