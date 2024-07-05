package org.websoso.WSSServer.exception.novel.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.novel.NovelErrorCode;

@Getter
@AllArgsConstructor
public class CustomNovelException extends RuntimeException {

    public CustomNovelException(NovelErrorCode novelErrorCode, String message) {
        super(message);
        this.novelErrorCode = novelErrorCode;
    }

    private NovelErrorCode novelErrorCode;
}
