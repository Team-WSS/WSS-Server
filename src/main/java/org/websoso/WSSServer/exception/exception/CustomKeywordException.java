package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.KeywordErrorCode;

@Getter
@AllArgsConstructor
public class CustomKeywordException extends RuntimeException {

    public CustomKeywordException(KeywordErrorCode keywordErrorCode, String message) {
        super(message);
        this.keywordErrorCode = keywordErrorCode;
    }

    private KeywordErrorCode keywordErrorCode;
}
