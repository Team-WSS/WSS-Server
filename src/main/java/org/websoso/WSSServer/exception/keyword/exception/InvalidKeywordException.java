package org.websoso.WSSServer.exception.keyword.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.keyword.KeywordErrorCode;

@Getter
@AllArgsConstructor
public class InvalidKeywordException extends RuntimeException {

    public InvalidKeywordException(KeywordErrorCode keywordErrorCode, String message) {
        super(message);
        this.keywordErrorCode = keywordErrorCode;
    }

    private KeywordErrorCode keywordErrorCode;
}
