package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportedType {

    SPOILER("spoiler", "스포일러", 3),
    IMPERTINENCE("impertinence", "부적절한 표현", 3);

    private final String label;
    private final String description;
    private final int reportLimit;

    public boolean isExceedingLimit(int reportedCount) {
        return reportedCount >= reportLimit;
    }
}
