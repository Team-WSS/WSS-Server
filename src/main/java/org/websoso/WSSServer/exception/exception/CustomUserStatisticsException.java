package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomUserStatisticsError;

@Getter
@AllArgsConstructor
public class CustomUserStatisticsException extends RuntimeException {

    public CustomUserStatisticsException(CustomUserStatisticsError customUserStatisticsError, String message) {
        super(message);
        this.customUserStatisticsError = customUserStatisticsError;
    }

    private CustomUserStatisticsError customUserStatisticsError;
}
