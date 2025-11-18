package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomImageError.EMPTY_IMAGE_FILE;
import static org.websoso.WSSServer.exception.error.CustomImageError.IMAGE_FILE_IO;
import static org.websoso.WSSServer.exception.error.CustomImageError.INVALID_IMAGE_FILE_NAME;
import static org.websoso.WSSServer.exception.error.CustomImageError.UPLOAD_FAIL_FILE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;
import org.websoso.WSSServer.dto.feed.FeedImageDeleteEvent;
import org.websoso.WSSServer.exception.exception.CustomImageException;
import org.websoso.s3.core.S3FileService;
import org.websoso.s3.modle.S3UploadResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageClient {

    private static final String FEED_UPLOAD_DIRECTORY = "feed/";
    private static final String SOURCE_DEFAULT = "DEFAULT";
    private static final String SOURCE_AFTER_COMMIT = "AFTER_COMMIT";

    private final S3FileService s3FileService;

    public String uploadFeedImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            log.error("빈 이미지 파일은 업로드 할 수 없습니다.");
            throw new CustomImageException(EMPTY_IMAGE_FILE, "빈 이미지 파일은 업로드할 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            log.error("이미지 파일의 이름이 유효하지 않습니다. : {}", originalFilename);
            throw new CustomImageException(INVALID_IMAGE_FILE_NAME, "이미지 파일의 이름이 유효하지 않습니다.");
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
            return result.url();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CustomImageException(IMAGE_FILE_IO, "이미지 파일을 읽는 중 오류가 발생했습니다. 파일이 손상되었거나 존재하지 않습니다.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // TODO: 이미지 파일 업로드 실패 예외 처리는 라이브러리 Exception 전부 정의된 후 수정짓도록 하겠습니다.
            throw new CustomImageException(UPLOAD_FAIL_FILE, "이미지 업로드에 실패했습니다.");
        }
    }

    public void deleteImages(List<String> imageUrls) {
        deleteByUrls(imageUrls, SOURCE_DEFAULT);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleteEvent(FeedImageDeleteEvent event) {
        deleteByUrls(event.imageUrls(), SOURCE_AFTER_COMMIT);
    }

    private void deleteByUrls(List<String> imageUrls, String source) {
        for (String url : imageUrls) {
            try {
                String key = extractS3Key(url);
                s3FileService.delete(key);
            } catch (Exception e) {
                log.error("[{}] S3 이미지 삭제 실패 url = {}", source, url, e);
            }
        }
    }

    private String extractS3Key(String url) {
        URI uri = URI.create(url);
        return uri.getPath().substring(1);
    }
}
