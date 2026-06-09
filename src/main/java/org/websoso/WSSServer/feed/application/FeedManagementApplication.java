package org.websoso.WSSServer.feed.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedCreateResponse;
import org.websoso.WSSServer.dto.feed.FeedImageCreateRequest;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.FeedImage;
import org.websoso.WSSServer.feed.service.FeedService;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.service.ImageClient;
import org.websoso.WSSServer.user.domain.User;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedManagementApplication {

    private final FeedService feedService;
    private final NovelServiceImpl novelService;
    private final ImageClient imageUploader;

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
