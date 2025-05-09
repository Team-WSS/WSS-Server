package org.websoso.WSSServer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;
import org.websoso.WSSServer.dto.feed.FeedImageDeleteEvent;
import org.websoso.s3.core.S3FileService;
import org.websoso.s3.modle.S3UploadResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

//TODO: Exception 수정해야함
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private static final String FEED_UPLOAD_DIRECTORY = "feed/";

    private final S3FileService s3FileService;

    public String uploadFeedImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("유효하지 않은 파일 이름입니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')); // 예: ".jpg"
        String imageName = UUID.randomUUID() + extension;
        String key = FEED_UPLOAD_DIRECTORY + imageName;

        try (InputStream inputStream = file.getInputStream()) {
            S3UploadResult result = s3FileService.upload(
                    key,
                    inputStream,
                    file.getContentType(),
                    file.getSize()
            );

            // TODO: Library의 로직 문제로 수정시 리팩토링 예정입니다.
            return result.message();
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 업로드 중 오류가 발생했습니다.", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteImages(FeedImageDeleteEvent event) {
        for (String url : event.imageUrls()) {
            try {
                String key = extractS3Key(url);
                s3FileService.delete(key);
            } catch (Exception e) {
                log.error("S3 이미지 삭제 실패. url = {}", url, e);
            }
        }
    }

    private String extractS3Key(String url) {
        URI uri = URI.create(url);
        return uri.getPath().substring(1);
    }
}
