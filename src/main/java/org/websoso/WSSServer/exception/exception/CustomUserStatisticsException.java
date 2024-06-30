package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.UserStatisticsErrorCode;

@Getter
@AllArgsConstructor
public class CustomUserStatisticsException extends RuntimeException {

    public CustomUserStatisticsException(UserStatisticsErrorCode userStatisticsErrorCode, String message) {
        super(message);
        this.userStatisticsErrorCode = userStatisticsErrorCode;
    }

    private UserStatisticsErrorCode userStatisticsErrorCode;
}
