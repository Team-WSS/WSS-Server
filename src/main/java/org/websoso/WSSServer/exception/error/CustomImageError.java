package org.websoso.WSSServer.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum CustomImageError implements ICustomError {

    UPLOAD_FAIL_FILE("IMAGE-001", "이미지 업로드에 실패했습니다.", INTERNAL_SERVER_ERROR),
    EMPTY_IMAGE_FILE("IMAGE-002", "빈 이미지 파일은 업로드할 수 없습니다.", BAD_REQUEST),
    IMAGE_FILE_IO("IMAGE-003", "이미지 파일을 읽는 중 오류가 발생했습니다. 파일이 손상되었거나 존재하지 않습니다.", INTERNAL_SERVER_ERROR),
    INVALID_IMAGE_FILE_NAME("IMAGE-004", "이미지 파일의 이름이 유효하지 않습니다.", BAD_REQUEST),
    INVALID_IMAGE_FILE_TYPE("IMAGE-005", "이미지 파일의 타입이 유효하지 않습니다.", BAD_REQUEST),
    ;

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
