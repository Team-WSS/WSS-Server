package org.websoso.WSSServer.dto.feed;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record FeedImageUpdateRequest(
        @Size(max = 20, message = "피드 이미지는 20개를 초과할 수 없습니다.")
        List<MultipartFile> images
) {
}
