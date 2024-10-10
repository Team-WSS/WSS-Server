package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomKakaoError;

@Getter
public class CustomKakaoException extends AbstractCustomException {

    public CustomKakaoException(CustomKakaoError customKakaoError, String message) {
        super(customKakaoError, message);
    }
}
