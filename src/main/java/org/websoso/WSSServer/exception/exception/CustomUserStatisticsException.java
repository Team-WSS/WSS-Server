package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomUserStatisticsError;

@Getter
public class CustomUserStatisticsException extends AbstractCustomException {

    public CustomUserStatisticsException(CustomUserStatisticsError customUserStatisticsError, String message) {
        super(customUserStatisticsError, message);
    }
}
