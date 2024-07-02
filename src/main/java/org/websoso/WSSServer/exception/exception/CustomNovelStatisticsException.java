package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomNovelStatisticsError;

@Getter
public class CustomNovelStatisticsException extends AbstractCustomException {

    public CustomNovelStatisticsException(CustomNovelStatisticsError customNovelStatisticsError, String message) {
        super(customNovelStatisticsError, message);
    }
}
