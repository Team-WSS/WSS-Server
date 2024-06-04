package org.websoso.WSSServer.exception.novelStatistics.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode;

@Getter
@AllArgsConstructor
public class InvalidNovelStatisticsException extends RuntimeException {

    public InvalidNovelStatisticsException(NovelStatisticsErrorCode novelStatisticsErrorCode, String message) {
        super(message);
        this.novelStatisticsErrorCode = novelStatisticsErrorCode;
    }

    private NovelStatisticsErrorCode novelStatisticsErrorCode;
}
