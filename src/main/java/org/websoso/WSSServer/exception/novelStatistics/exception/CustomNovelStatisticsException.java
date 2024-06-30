package org.websoso.WSSServer.exception.novelStatistics.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode;

@Getter
@AllArgsConstructor
public class CustomNovelStatisticsException extends RuntimeException {

    public CustomNovelStatisticsException(NovelStatisticsErrorCode novelStatisticsErrorCode, String message) {
        super(message);
        this.novelStatisticsErrorCode = novelStatisticsErrorCode;
    }

    private NovelStatisticsErrorCode novelStatisticsErrorCode;

}
