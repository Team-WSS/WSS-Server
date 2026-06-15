package org.websoso.WSSServer.feed.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.websoso.WSSServer.feed.domain.FeedImage;
import org.websoso.WSSServer.service.ImageClient;

@Service
@RequiredArgsConstructor
public class FeedImageService {

    private final ImageClient imageUploader;

    public List<FeedImage> processFeedImages(List<MultipartFile> images) {
        List<String> uploadedImageUrls = uploadFeedImages(images);

        return createFeedImages(uploadedImageUrls);
    }

    private List<String> uploadFeedImages(List<MultipartFile> images) {
        List<String> uploadedImageUrls = new ArrayList<>();

        if (images == null || images.isEmpty()) {
            return uploadedImageUrls;
        }

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

        return uploadedImageUrls;
    }

    private List<FeedImage> createFeedImages(List<String> uploadedImageUrls) {
        List<FeedImage> feedImages = new ArrayList<>();

        if (uploadedImageUrls.isEmpty()) {
            return feedImages;
        }

        feedImages.add(FeedImage.createThumbnail(uploadedImageUrls.get(0)));
        for (int i = 1; i < uploadedImageUrls.size(); i++) {
            feedImages.add(FeedImage.createCommon(uploadedImageUrls.get(i), i));
        }

        return feedImages;
    }

}
