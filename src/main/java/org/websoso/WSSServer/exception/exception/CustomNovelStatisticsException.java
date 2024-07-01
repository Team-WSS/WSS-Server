package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomNovelStatisticsError;

@Getter
@AllArgsConstructor
public class CustomNovelStatisticsException extends RuntimeException {

    public CustomNovelStatisticsException(CustomNovelStatisticsError customNovelStatisticsError, String message) {
        super(message);
        this.customNovelStatisticsError = customNovelStatisticsError;
    }

    private CustomNovelStatisticsError customNovelStatisticsError;

}
